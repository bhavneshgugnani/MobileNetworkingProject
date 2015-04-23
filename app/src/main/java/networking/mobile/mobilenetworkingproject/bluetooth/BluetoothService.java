package networking.mobile.mobilenetworkingproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import networking.mobile.mobilenetworkingproject.backup.DataBackupReaderWriter;
import networking.mobile.mobilenetworkingproject.bluetooth.abstractservice.Service;
import networking.mobile.mobilenetworkingproject.constant.Constants;
import networking.mobile.mobilenetworkingproject.file.FileReaderWriter;

/**
 * Created by Bhavnesh Gugnani on 4/16/2015.
 */
public class BluetoothService implements Service {
    // Debugging
    private static final String TAG = "BluetoothService";

    // Member fields
    private BluetoothAdapter mAdapter = null;
    private Context context = null;
    private Handler handler = null;
    private AcceptThread mAcceptThread = null;
    private ConnectThread mConnectThread = null;
    private ConnectedThread mConnectedThread = null;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     */
    public BluetoothService(BluetoothAdapter mAdapter, Context context, Handler handler) {
        this.mAdapter = mAdapter;
        mState = STATE_NONE;
        this.context = context;
        this.handler = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Override
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    @Override
    public synchronized void connect(BluetoothDevice device, byte[] out) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, out);
        //mConnectThread.start();
        mConnectThread.run();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Override
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, byte[] out) {
        Log.d(TAG, "connected, Socket Type");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, out);
        //mConnectedThread.start();
        mConnectedThread.run();

        // Send the name of the connected device back to the UI Activity
        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.DEVICE_NAME, device.getName());
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    @Override
    public void write(final BluetoothDevice device, final byte[] out) {
        //connect to device
        new Thread() {
            @Override
            public void run() {
                synchronized (BluetoothService.class) {
                    connect(device, out);

                }

            }
        }.start();
    }

    @Override
    public void syncDataToNetwork(byte[] text){
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            write(device, text);
        }

        //sync data by scanning new devices if found in scanning
        discoverMoreDevices(text);
    }

    private void discoverMoreDevices(final byte[] text) {
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    CharSequence msg = "Device found : " + device.getName();
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    // write to new devices
                    write(device, text);

                }
            }
        };
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        //start discovery
        mAdapter.startDiscovery();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.TOAST, "Unable to connect device");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.TOAST, "Device connection was lost");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        //private String mSocketType;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            //mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                // APP_UUID is the app's UUID string, also used by the client code
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(Constants.APP_NAME, Constants.APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket Type listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: BEGIN mAcceptThread" + this);
            setName("AcceptThread");

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: accept() failed");
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), null);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            //Log.i(TAG, "END mAcceptThread, socket Type");
        }

        public void cancel() {
            Log.d(TAG, "Socket Type cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private byte[] out;
        //private String mSocketType;

        public ConnectThread(BluetoothDevice device, byte[] out) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            this.out = out;
            //mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(Constants.APP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread Socket");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, out);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] out;

        public ConnectedThread(BluetoothSocket socket, byte[] out) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            this.out = out;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            if (out != null) {
                try {
                    this.write(out);
                    sendMessageToHandler("Data Synced to remote device " + mmSocket.getRemoteDevice().getName(), true);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }

                this.cancel();
                Log.d(TAG, "Syncing of data " + new String(out) + " complete with " + mmSocket.getRemoteDevice());
                //restart accept thread
                connectionLost();
                BluetoothService.this.start();
                return;
            } else {
                byte[] buffer = new byte[1024];
                int bytes;
                BluetoothDevice device = mmSocket.getRemoteDevice();
                String remoteDeviceName = device.getName();
                String text = "";

                // Keep listening to the InputStream while connected
                while (true) {

                    try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        //store backup text
                        text += new String(buffer);
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);

                        break;
                    } finally {
                        //write backup to local memory
                        if (out == null) {//data received
                            DataBackupReaderWriter.writeNetworkBackupToLocalMemory(context, remoteDeviceName, text.trim());
                            Log.d(TAG, "Backup text : " + text.trim() + "written to file : " + remoteDeviceName + ".txt");
                            sendMessageToHandler("Data received from " + remoteDeviceName, false);
                        }
                        connectionLost();
                        // Start the service over to restart listening mode
                        //BluetoothService.this.start();
                    }
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) throws IOException {
            mmOutStream.write(buffer);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private void sendMessageToHandler(String message, boolean isDataSynced) {
        Message msgObj = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.HANDLER_MSG, message);
        bundle.putString(Constants.HANDLER_MSG_KEY, Boolean.toString(isDataSynced));
        msgObj.setData(bundle);
        handler.sendMessage(msgObj);
    }
}
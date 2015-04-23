package networking.mobile.mobilenetworkingproject.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Bhavnesh Gugnani on 4/7/2015.
 */
public class ConnectThread implements Runnable {
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final UUID uuid;

    public ConnectThread(BluetoothDevice device, UUID uuid, BluetoothAdapter mBluetoothAdapter) {
        this.mmDevice = device;
        BluetoothSocket tmp = null;
        this.uuid = uuid;
        this.mBluetoothAdapter = mBluetoothAdapter;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            //Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            System.out.println("Socket Type create() failed");
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {

    }
}

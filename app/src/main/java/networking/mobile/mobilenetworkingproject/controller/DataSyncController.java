package networking.mobile.mobilenetworkingproject.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.util.Set;

import networking.mobile.mobilenetworkingproject.bluetooth.BluetoothService;
import networking.mobile.mobilenetworkingproject.constant.Constants;
import networking.mobile.mobilenetworkingproject.file.FileReaderWriter;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

/**
 * Created by Bhavnesh Gugnani on 4/6/2015.
 */
public class DataSyncController {
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService bluetoothService = null;
    private Context context = null;
    private BroadcastReceiver mReceiver = null;
    private Handler handler = null;

    public DataSyncController(BluetoothAdapter mBluetoothAdapter, Context context, Handler handler) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = context;
        this.handler = handler;
        this.bluetoothService = new BluetoothService(mBluetoothAdapter, context, handler);
    }

    public void start() {
        bluetoothService.start();
    }

    public void stop() {
        bluetoothService.stop();
    }

    public void syncDataToNetwork() {
        if (ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)) {
            //update network with new data
            while (ApplicationState.pendingDataSyncedToNetwork)
                clearAnyPendingSyncToNetwork();
        }
    }

    public void clearAnyPendingSyncToNetwork() {
        if (ApplicationState.pendingDataSyncedToNetwork) {
            byte[] text = getPendingData().getBytes();
            //sync data with network
            bluetoothService.syncDataToNetwork(text);

        } else {
            CharSequence text = "All data already synced to network";
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    /*private void discoverMoreDevices(final byte[] text) {
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
                    bluetoothService.write(device, text);

                }
            }
        };
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        //start discovery
        mBluetoothAdapter.startDiscovery();
    }*/

    private String getPendingData() {
        String fileText = "";
        try {
            fileText += FileReaderWriter.readFileFromMemory(context, context.openFileInput(FileReaderWriter.STORAGE_FILENAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileText.trim();
    }

    public void destroy() {
        if(mReceiver != null)
            context.unregisterReceiver(mReceiver);
        bluetoothService.stop();
    }


}

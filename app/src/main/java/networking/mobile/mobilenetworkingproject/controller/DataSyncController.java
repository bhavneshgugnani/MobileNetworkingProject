package networking.mobile.mobilenetworkingproject.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;

import networking.mobile.mobilenetworkingproject.bluetooth.BluetoothBleService;
import networking.mobile.mobilenetworkingproject.bluetooth.BluetoothService;
import networking.mobile.mobilenetworkingproject.bluetooth.abstractservice.Service;
import networking.mobile.mobilenetworkingproject.file.FileReaderWriter;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

/**
 * Created by Bhavnesh Gugnani on 4/6/2015.
 */
public class DataSyncController {
    private BluetoothAdapter mBluetoothAdapter = null;
    private Service bluetoothService = null;
    private Context context = null;
    private Handler handler = null;

    public DataSyncController(BluetoothAdapter mBluetoothAdapter, Context context, Handler handler) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = context;
        this.handler = handler;
        if(ApplicationState.bluetoothSetting.equalsIgnoreCase(ApplicationState.BLUETOOTH))
            this.bluetoothService = new BluetoothService(mBluetoothAdapter, context, handler);
        else if(ApplicationState.bluetoothSetting.equalsIgnoreCase(ApplicationState.BLUETOOTH_BLE))
            this.bluetoothService = new BluetoothBleService(mBluetoothAdapter, context, handler);
    }

    public void start() {
        bluetoothService.start();
        //perform pending sync if any
        syncDataToNetwork();
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
        if(ApplicationState.pendingDataSyncedToNetwork) {
            byte[] text = getPendingData().getBytes();
            //sync data with network
            bluetoothService.syncDataToNetwork(text);
        }
    }

    private String getPendingData() {
        String fileText = "";
        try {
            fileText += FileReaderWriter.readFileFromMemory(context, context.openFileInput(FileReaderWriter.STORAGE_FILENAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileText.trim();
    }
}
package networking.mobile.mobilenetworkingproject.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import networking.mobile.mobilenetworkingproject.bluetooth.abstractservice.Service;

/**
 * Created by Bhavnesh Gugnani on 4/23/2015.
 */
public class BluetoothBleService implements Service {
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private Handler mHandler = null;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Context context;
    //private BluetoothAdapter.LeScanCallback mLeScanCallback = null;

    public BluetoothBleService(BluetoothAdapter mBluetoothAdapter, Context context, Handler handler) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = context;
        // Device scan callback.

    }

    @Override
    public void start() {
        mHandler = new Handler();
    }

    public void syncDataToNetwork(final byte[] text) {
        BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                try {
                    CharSequence msg = "Device found";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    write(device, text);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        });*/
            }
        };
        scanLeDevice(true, mLeScanCallback);
    }

    @Override
    public void connect(BluetoothDevice device, byte[] out) {

    }

    @Override
    public void connected(BluetoothSocket socket, BluetoothDevice device, byte[] out) {

    }

    @Override
    public void stop() {
    }

    @Override
    public void write(BluetoothDevice device, byte[] out) throws IOException {
        //BluetoothGattCallback mGattCallback = null;
        //BluetoothGatt mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
    }

    private void scanLeDevice(final boolean enable, final BluetoothAdapter.LeScanCallback mLeScanCallback) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
}

package networking.mobile.mobilenetworkingproject.service;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.MainActivity;
import networking.mobile.mobilenetworkingproject.R;
import networking.mobile.mobilenetworkingproject.broadcastreceivers.IntervalScanningBroadcastReceiver;
import networking.mobile.mobilenetworkingproject.constant.Constants;
import networking.mobile.mobilenetworkingproject.controller.DataSyncController;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SyncingService extends Service {
    private static final String SCANNING_SETTING = "scanningsetting";
    private static final String INTERVAL_SETTING = "intervalsetting";
    private static final String BLUETOOTH_SETTING = "bluetoothsetting";

    public static final String ADJUST_SCANNING = "mobilenetworking.adjustscanning";
    private static final int DISCOVERABILITY_TIME = 0;//Always discoverable
    private static final int START_DEVICE_BLUETOOTH_DISCOVERABILITY = 800;

    private BluetoothAdapter mBluetoothAdapter = null;
    private DataSyncController syncController = null;
    private AlarmManager aManager = null;
    private Handler handler = null;
    private IntervalScanningBroadcastReceiver iReceiver = null;
    private IntentFilter iFilter = null;

    public static void startService(Context context) {
        Intent intent = new Intent(context, SyncingService.class);
        intent.putExtra(SCANNING_SETTING, ApplicationState.scanningFrequencySetting);
        intent.putExtra(INTERVAL_SETTING, ApplicationState.interval);
        intent.putExtra(BLUETOOTH_SETTING, ApplicationState.bluetoothSetting);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, SyncingService.class);
        context.stopService(intent);
    }

    public SyncingService() {
        super();
    }

    @Override
    public void onCreate() {
        mBluetoothAdapter = getBluetoothAdapter();
        createHandler();
        syncController = new DataSyncController(mBluetoothAdapter, getApplicationContext(), handler);
        aManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //register broadcast receiver for interval scanning
        iReceiver = new IntervalScanningBroadcastReceiver(getApplicationContext(), syncController, aManager);
        iFilter = new IntentFilter(ADJUST_SCANNING);
        registerReceiver(iReceiver, iFilter);

        ApplicationState.joinedNetwork = true;

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncController.start();
        Intent updateIntent = new Intent();
        updateIntent.setAction(ApplicationState.scanningFrequencySetting);
        sendBroadcast(updateIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(iReceiver);
        syncController.stop();

        //Disable bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }

        ApplicationState.joinedNetwork = false;

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (ApplicationState.bluetoothSetting == ApplicationState.BLUETOOTH)
            return BluetoothAdapter.getDefaultAdapter();//bluetooth adapter
        else {
            //bluetooth_ble adapter
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                return null;
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            return bluetoothManager.getAdapter();
        }
    }

    public void createHandler() {
        this.handler = new Handler() {
            public void handleMessage(Message msg) {
                String message = msg.getData().getString(Constants.HANDLER_MSG).trim();
                if (message != null && message != "") {
                    CharSequence text = message;
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }

                if (Boolean.parseBoolean(msg.getData().getString(Constants.HANDLER_MSG_KEY).trim())) {//updates synced to network
                    // reset flag for data sync to network.
                    if (ApplicationState.pendingDataSyncedToNetwork) {
                        ApplicationState.pendingDataSyncedToNetwork = false;
                        CharSequence text = "All data Synced to Network";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                        //setPendingSyncState(ApplicationState.pendingDataSyncedToNetwork);
                    }
                }
            }
        };
    }

    private void openBluetoothNotSupportedDialog() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_bluetooth_not_supported).setMessage(R.string.message_bluetooth_not_supported)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SyncingService.stopService(getApplicationContext());
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();*/
    }
}

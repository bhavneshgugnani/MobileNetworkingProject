package networking.mobile.mobilenetworkingproject;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.backup.ViewBackupActivity;
import networking.mobile.mobilenetworkingproject.broadcastreceivers.ServiceBroadcastReceiver;
import networking.mobile.mobilenetworkingproject.file.EditFileActivity;
import networking.mobile.mobilenetworkingproject.service.SyncingService;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

public class MainActivity extends ActionBarActivity {

    private static SharedPreferences sharedPreferences = null;

    private static final int SCANNER_FREQUENCY_SETINGS_REQUST_CODE = 100;
    private static final int START_DEVICE_BLUETOOTH_DISCOVERABILITY = 800;
    private static final String SCANNING_FREQUENCY_PREFERENCE = "scanningfrequencypreference";
    private static final String INTERVAL_PREFERENCE = "intervalpreference";
    private static final String BLUETOOTH_PREFERENCE = "bluetoothpreference";
    private static final String PENDING_SYNC_TO_NETWORK = "pendingsynctonetwork";
    private static final String JOINED_NETWORK = "joinednetwork";

    private static final int EDIT_FILE_REQUEST_CODE = 500;

    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int DISCOVERABILITY_TIME = 0;//Always discoverable
    private static final int MANUAL_SCAN_MENU_ITEM_INDEX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign default string values to constants
        assignDefaultStringValues();

        //load settings from shared preferences
        loadSettingsFromSharedPreferences();

        mBluetoothAdapter = getBluetoothAdapter();



    }

    @Override
    protected void onResume() {
        super.onResume();
        //set default syn state
        setPendingSyncState(ApplicationState.pendingDataSyncedToNetwork);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING))
            menu.getItem(MANUAL_SCAN_MENU_ITEM_INDEX).setEnabled(true);
        else
            menu.getItem(MANUAL_SCAN_MENU_ITEM_INDEX).setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.title_manual_scan) {
            //clear pending sync to network
            sendManulSyncIntent();
        } else if (id == R.id.title_scanner_frequency_settings) {
            openScannerFrequencySettingsDialog();
        } else if (id == R.id.title_interval_scanning_settings) {
            openIntervalScanningSettingsDialog();
        } else if (id == R.id.title_bluetooth_settings) {
            openBluetoothSettingsDialog();
        } else if (id == R.id.view_network_backup_data) {
            viewBackupDataFiles();
        } else if (id == R.id.join_network) {
            enableBluetoothAndDiscoverability();
            //service is started after input from user is obtained if bluetooth permission is allowed or not.
        } else if (id == R.id.leave_network) {
            SyncingService.stopService(getApplicationContext());
        } else if (id == R.id.exit_app) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    private void enableBluetoothAndDiscoverability() {

        if (mBluetoothAdapter != null) {
            //enable discoverability of device, also enables bluetooth in device
            enableDiscoverability();
        } else {
            //Bluetooth not supported
            openBluetoothNotSupportedDialog();
        }
    }

    private void enableDiscoverability() {
        //bluetooth is automatically enabled if made discoverable, which is also required
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABILITY_TIME);
        startActivityForResult(discoverableIntent, START_DEVICE_BLUETOOTH_DISCOVERABILITY);
    }

    private void assignDefaultStringValues(){
        //assign default values
        ApplicationState.CONTINUOUS_SCANNING_SETTING = getResources().getString(R.string.continuousscanning);
        ApplicationState.INTERVAL_SCANNING_SETTING = getResources().getString(R.string.intervalscanning);
        ApplicationState.MANUAL_SCANNING_SETTING = getResources().getString(R.string.manualscanning);
        ApplicationState.BLUETOOTH = getResources().getString(R.string.bluetooth);
        ApplicationState.BLUETOOTH_BLE = getResources().getString(R.string.bluetooth_ble);
    }

    private void loadSettingsFromSharedPreferences() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //these settings can only be changed by UI, so reading from last stored sharedpreferences
        ApplicationState.scanningFrequencySetting = sharedPreferences.getString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.defaultScanningFrequencySetting);
        ApplicationState.interval = sharedPreferences.getInt(INTERVAL_PREFERENCE, ApplicationState.defaultInterval);
        ApplicationState.bluetoothSetting = sharedPreferences.getString(BLUETOOTH_PREFERENCE, ApplicationState.defaultBluetoothSetting);

        ApplicationState.joinedNetwork = sharedPreferences.getBoolean(JOINED_NETWORK, ApplicationState.defaultJoinedNetwork);

        //if service is already running, the sharedpreference value in this case might be outdated. Application state would already have been updated in case sync complete
        if (!ApplicationState.joinedNetwork)
            ApplicationState.pendingDataSyncedToNetwork = sharedPreferences.getBoolean(PENDING_SYNC_TO_NETWORK, ApplicationState.defaultPendingSyncToNetwork);

    }

    private void openScannerFrequencySettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final String[] allScanningFrequencySettings = {ApplicationState.CONTINUOUS_SCANNING_SETTING, ApplicationState.INTERVAL_SCANNING_SETTING, ApplicationState.MANUAL_SCANNING_SETTING};
        builder.setTitle(R.string.scanner_frequency_settings)
                .setItems(allScanningFrequencySettings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (allScanningFrequencySettings[which].equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)) {
                            //Continuous Scanning chosen
                            if (!ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)) {
                                ApplicationState.scanningFrequencySetting = ApplicationState.CONTINUOUS_SCANNING_SETTING;
                                ApplicationState.hasSettingsChanged = true;
                            }
                        } else if (allScanningFrequencySettings[which].equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)) {
                            //Interval scanning chosen
                            if (!ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)) {
                                ApplicationState.scanningFrequencySetting = ApplicationState.INTERVAL_SCANNING_SETTING;
                                ApplicationState.hasSettingsChanged = true;
                            }
                        } else if (allScanningFrequencySettings[which].equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING)) {
                            //Manual scanning chosen
                            if (!ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING)) {
                                ApplicationState.scanningFrequencySetting = ApplicationState.MANUAL_SCANNING_SETTING;
                                ApplicationState.hasSettingsChanged = true;
                            }
                        }
                        //send intent to service to adjust scanning
                        Intent updateIntent = new Intent();
                        updateIntent.setAction(ServiceBroadcastReceiver.ADJUST_SCANNING_INTENT);
                        sendBroadcast(updateIntent);

                        CharSequence text = getResources().getString(R.string.new_scanner_frequency_setting_saved);
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void openIntervalScanningSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final EditText intervalInput = new EditText(this);
        intervalInput.setText(String.valueOf(ApplicationState.interval));
        builder.setView(intervalInput);
        builder.setTitle(R.string.interval_setting).setMessage(R.string.Interval_in_min)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int interval = Integer.parseInt(intervalInput.getText().toString().trim());
                        if (ApplicationState.interval != interval) {
                            ApplicationState.interval = interval;
                            ApplicationState.hasSettingsChanged = true;
                        }
                        //send intent to service to adjust scanning
                        Intent updateIntent = new Intent();
                        updateIntent.setAction(ServiceBroadcastReceiver.ADJUST_SCANNING_INTENT);
                        sendBroadcast(updateIntent);

                        CharSequence text = getResources().getString(R.string.new_interval_value_saved);
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void openBluetoothSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final String[] bluetoothSettings = {ApplicationState.BLUETOOTH, ApplicationState.BLUETOOTH_BLE};
        builder.setTitle(R.string.bluetooth_settings)
                .setItems(bluetoothSettings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = null;
                        if (bluetoothSettings[which].equalsIgnoreCase(ApplicationState.BLUETOOTH)) {
                            // Regular Bluetooth selected
                            if (!ApplicationState.bluetoothSetting.equalsIgnoreCase(ApplicationState.BLUETOOTH)) {
                                selected = ApplicationState.BLUETOOTH;
                            }
                        } else if (bluetoothSettings[which].equalsIgnoreCase(ApplicationState.BLUETOOTH_BLE)) {
                            // Bluetooth BLE selected
                            if (!ApplicationState.bluetoothSetting.equalsIgnoreCase(ApplicationState.BLUETOOTH_BLE)) {
                                selected = ApplicationState.BLUETOOTH_BLE;
                            }
                        }
                        if (selected != null) {
                            if (bluetoothSelectedSettingSupported(selected)) {
                                CharSequence text = getResources().getString(R.string.new_bluetooth_setting_saved);
                                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                                //adjust bluetooth settings
                                adjustBluetoothSettings();
                            } else {
                                openBluetoothSelectedSettingNotSupportedDialog();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void adjustBluetoothSettings() {
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
        enableBluetoothAndDiscoverability();
    }

    private boolean bluetoothSelectedSettingSupported(String selected) {
        boolean result = false;
        if (selected == ApplicationState.BLUETOOTH) {
            if (BluetoothAdapter.getDefaultAdapter() == null)
                result = false;
            else
                result = true;
        } else if (selected == ApplicationState.BLUETOOTH_BLE) {
            // Implementation of BLE support is still pending, complete implementation is not working. For now, only bluetooth supported
            result = false;
            /*if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                result = true;
            else
                result = false;*/
        }
        return result;
    }

    private void openBluetoothNotSupportedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_bluetooth_not_supported).setMessage(R.string.message_bluetooth_not_supported)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    private void openBluetoothSelectedSettingNotSupportedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_bluetooth_not_supported).setMessage(R.string.message_selected_bluetooth_not_supported)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Do not save settings
                    }
                });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCANNER_FREQUENCY_SETINGS_REQUST_CODE) {
            //scanner frequency settings saved
        } else if (requestCode == EDIT_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //synchronise changes to file in network
                setPendingSyncState(ApplicationState.pendingDataSyncedToNetwork);
                //synchroniseChangesToNetwork();
            } else if (resultCode == RESULT_CANCELED) {
                //no changes to file
            }
        } else if (requestCode == START_DEVICE_BLUETOOTH_DISCOVERABILITY) {
            //bluetooth discoverability enabled
            if (resultCode == DISCOVERABILITY_TIME + 1) {//android returns 1 when it should return 0
                startSyncingService();
            } else if (resultCode == RESULT_CANCELED) {
                //bluetooth discoverabiltiy diabled
                CharSequence msg = getResources().getString(R.string.bluetooth_permission_denied);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        //Save any changes in settings
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        if (ApplicationState.hasSettingsChanged){
            editor.putString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.scanningFrequencySetting);
            editor.putInt(INTERVAL_PREFERENCE, ApplicationState.interval);
            editor.putString(BLUETOOTH_PREFERENCE, ApplicationState.bluetoothSetting);
        }
        editor.putBoolean(PENDING_SYNC_TO_NETWORK, ApplicationState.pendingDataSyncedToNetwork);
        editor.putBoolean(JOINED_NETWORK, ApplicationState.joinedNetwork);

        // Commit to storage
        editor.commit();

        super.onDestroy();
    }

    private void startSyncingService() {
        //start background syncing service
        SyncingService.startService(getApplicationContext());
    }

    public void editFile(View view) {
        Intent intent = new Intent(this, EditFileActivity.class);
        startActivityForResult(intent, EDIT_FILE_REQUEST_CODE);
    }

    private void viewBackupDataFiles() {
        Intent intent = new Intent(this, ViewBackupActivity.class);
        startActivity(intent);
    }

    public void syncDataToNetwork(View view) {
        //send intent to service for manual sync in case service is running
        sendManulSyncIntent();
    }

    private void sendManulSyncIntent() {
        //sync data to network by sending intent to service if running
        if (ApplicationState.joinedNetwork) {
            Intent manualSyncIntent = new Intent();
            manualSyncIntent.setAction(ServiceBroadcastReceiver.MANUAL_UPDATE_INTENT);
            sendBroadcast(manualSyncIntent);
        } else {
            CharSequence msg = getResources().getString(R.string.join_network_msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }
    }

    private void setPendingSyncState(boolean pendingSyncState) {
        if (pendingSyncState)
            enableSyncButton();
        else
            disableSyncButton();
    }

    private void enableSyncButton() {
        Button view = (Button) findViewById(R.id.sync_button);
        view.setEnabled(true);
        view.setText(getString(R.string.sync_data));
    }

    private void disableSyncButton() {
        Button view = (Button) findViewById(R.id.sync_button);
        view.setEnabled(false);
        view.setText(getString(R.string.data_synced));
    }
}
package networking.mobile.mobilenetworkingproject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.broadcastreceivers.IntervalScanningBroadcastReceiver;
import networking.mobile.mobilenetworkingproject.controller.DataSyncController;
import networking.mobile.mobilenetworkingproject.file.EditFileActivity;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

public class MainActivity extends ActionBarActivity {

    private static SharedPreferences sharedPreferences = null;

    private static final int SCANNER_FREQUENCY_SETINGS_REQUST_CODE = 100;
    private static final String SCANNING_FREQUENCY_PREFERENCE = "scanningfrequencypreference";
    private static final String INTERVAL_PREFERENCE = "intervalpreference";

    private static final int EDIT_FILE_REQUEST_CODE = 500;

    private static BluetoothAdapter mBluetoothAdapter = null;
    private DataSyncController syncController = null;
    private AlarmManager aManager = null;
    private static final String ADJUST_SCANNING = "mobilenetworking.adjustscanning";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int DISCOVERABILITY_TIME = 0;//Always discoverable
    private static final int MANUAL_SCAN_MENU_ITEM_INDEX = 0;
    private static final int ONE_MINUTE_IN_MILLISECOND = 60000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        syncController = new DataSyncController();
        aManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //load settings from shared preferences
        loadSettingsFromSharedPreferences();

        //register broadcast receiver for interval scanning
        IntervalScanningBroadcastReceiver iReceiver = new IntervalScanningBroadcastReceiver(syncController);
        registerReceiver(iReceiver, new IntentFilter(ADJUST_SCANNING));

        //adjust scanning with new settings
        adjustScanningWithNewSettings();

        //enable bluetooth
        enableBluetoothAndDiscoverability();

        //perform pending sync if any
        syncController.clearAnyPendingSyncToNetwork();

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
            syncController.clearAnyPendingSyncToNetwork();
        } else if (id == R.id.title_scanner_frequency_settings) {
            openScannerFrequencySettingsDialog();
        } else if (id == R.id.title_interval_scanning_settings) {
            openIntervalScanningSettingsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableBluetoothAndDiscoverability() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            //enable bluetooth
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                //result received in onActivityResult()
                //Instead of enabling bluetooth, we can enable discoverability : http://developer.android.com/guide/topics/connectivity/bluetooth.html#EnablingDiscoverability
            }
        } else {
            //Bluetooth not supported
            openBluetoothNotSupportedDialog();
        }
        enableDiscoverability();
    }

    private void enableDiscoverability() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABILITY_TIME);
        startActivity(discoverableIntent);
    }

    private void loadSettingsFromSharedPreferences() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        ApplicationState.scanningFrequencySetting = sharedPreferences.getString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.defaultScanningFrequencySetting);
        ApplicationState.interval = sharedPreferences.getInt(INTERVAL_PREFERENCE, ApplicationState.defaultInterval);

        ApplicationState.CONTINUOUS_SCANNING_SETTING = getResources().getString(R.string.continuousscanning);
        ApplicationState.INTERVAL_SCANNING_SETTING = getResources().getString(R.string.intervalscanning);
        ApplicationState.MANUAL_SCANNING_SETTING = getResources().getString(R.string.manualscanning);
    }

    private void adjustScanningWithNewSettings() {
        if (ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)) {
            syncController.clearAnyPendingSyncToNetwork();
        } else if (ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING)) {
            //update menu options
            invalidateOptionsMenu();
        } else if (ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)) {
            //cancel any old scanning
            Intent alarmIntent = new Intent(ADJUST_SCANNING);//(this, IntervalScanningBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            aManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            aManager.cancel(pendingIntent);

            //start new interval schedule for scanning
            alarmIntent = new Intent(ADJUST_SCANNING);//(this, IntervalScanningBroadcastReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            aManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ApplicationState.interval * ONE_MINUTE_IN_MILLISECOND, pendingIntent);
        }
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
                                ApplicationState.hasScanningFrequencySettingsChanged = true;
                            }
                        } else if (allScanningFrequencySettings[which].equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)) {
                            //Interval scanning chosen
                            if (!ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)) {
                                ApplicationState.scanningFrequencySetting = ApplicationState.INTERVAL_SCANNING_SETTING;
                                ApplicationState.hasScanningFrequencySettingsChanged = true;
                            }
                        } else if (allScanningFrequencySettings[which].equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING)) {
                            //Manual scanning chosen
                            if (!ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING)) {
                                ApplicationState.scanningFrequencySetting = ApplicationState.MANUAL_SCANNING_SETTING;
                                ApplicationState.hasScanningFrequencySettingsChanged = true;
                            }
                        }
                        //adjust scanning
                        adjustScanningWithNewSettings();

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
                            ApplicationState.hasIntervalChanged = true;
                        }
                        //adjust scanning
                        adjustScanningWithNewSettings();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCANNER_FREQUENCY_SETINGS_REQUST_CODE) {
            //scanner frequency settings saved
        } else if (requestCode == EDIT_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //synchronise changes to file in network
                synchroniseChangesToNetwork();
            } else if (resultCode == RESULT_CANCELED) {
                //no changes to file
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (ApplicationState.hasScanningFrequencySettingsChanged || ApplicationState.hasIntervalChanged) {
            //Save any changes in settings
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

            if (ApplicationState.hasScanningFrequencySettingsChanged)
                editor.putString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.scanningFrequencySetting);
            if (ApplicationState.hasIntervalChanged)
                editor.putInt(INTERVAL_PREFERENCE, ApplicationState.interval);

            // Commit to storage
            editor.commit();

            //disable bluetooth
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
        }

        super.onDestroy();
    }

    public void editFile(View view) {
        Intent intent = new Intent(this, EditFileActivity.class);
        startActivityForResult(intent, EDIT_FILE_REQUEST_CODE);
    }

    private void synchroniseChangesToNetwork() {
        syncController.syncDataToNetwork();
    }

}

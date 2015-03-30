package networking.mobile.mobilenetworkingproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.state.ApplicationState;

public class MainActivity extends ActionBarActivity {

    private static SharedPreferences sharedPreferences = null;

    private static final int SCANNER_FREQUENCY_SETINGS_REQUST_CODE = 100;
    private static final String SCANNING_FREQUENCY_PREFERENCE = "scanningfrequencypreference";
    private static final String INTERVAL_PREFERENCE = "intervalpreference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferences = getPreferences(MODE_PRIVATE);
        ApplicationState.scanningFrequencySetting = sharedPreferences.getString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.defaultScanningFrequencySetting);
        ApplicationState.interval = sharedPreferences.getInt(INTERVAL_PREFERENCE, ApplicationState.defaultInterval);

        ApplicationState.CONTINUOUS_SCANNING_SETTING = getResources().getString(R.string.continuousscanning);
        ApplicationState.INTERVAL_SCANNING_SETTING = getResources().getString(R.string.intervalscanning);
        ApplicationState.MANUAL_SCANNING_SETTING = getResources().getString(R.string.manualscanning);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.title_scanner_frequency_settings){
            openScannerFrequencySettingsDialog();
        } else if(id == R.id.title_interval_scanning_settings){
            openIntervalScanningSettingsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openScannerFrequencySettingsDialog(){
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

    private void openIntervalScanningSettingsDialog(){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==SCANNER_FREQUENCY_SETINGS_REQUST_CODE)
        {
            //scanner frequency settings saved
        }
    }

    @Override
    protected void onDestroy() {
        if(ApplicationState.hasScanningFrequencySettingsChanged || ApplicationState.hasIntervalChanged){
            //Save any changes in settings
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

            if(ApplicationState.hasScanningFrequencySettingsChanged)
                editor.putString(SCANNING_FREQUENCY_PREFERENCE, ApplicationState.scanningFrequencySetting);
            if(ApplicationState.hasIntervalChanged)
                editor.putInt(INTERVAL_PREFERENCE, ApplicationState.interval);

            // Commit to storage
            editor.commit();
        }

        super.onDestroy();
    }
}

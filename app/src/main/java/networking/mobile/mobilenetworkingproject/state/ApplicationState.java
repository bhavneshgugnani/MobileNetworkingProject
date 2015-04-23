package networking.mobile.mobilenetworkingproject.state;

/**
 * Created by Bhavnesh Gugnani on 3/28/2015.
 */
public class ApplicationState {

    public static String CONTINUOUS_SCANNING_SETTING;
    public static String INTERVAL_SCANNING_SETTING;
    public static String MANUAL_SCANNING_SETTING;

    public static String BLUETOOTH;
    public static String BLUETOOTH_BLE;

    public static String defaultScanningFrequencySetting = "Manual Scanning";
    public static int defaultInterval = 15;
    public static String defaultBluetoothSetting = "Bluetooth";

    public static String scanningFrequencySetting = null;
    public static int interval;
    public static String bluetoothSetting = null;

    //only for final commit to memory in case changed
    public static boolean hasScanningFrequencySettingsChanged = false;
    public static boolean hasIntervalChanged = false;
    public static boolean hasbluetoothSettingsChanged = false;

    public static boolean pendingDataSyncedToNetwork;
}

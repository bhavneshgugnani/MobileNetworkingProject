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

    public static final String defaultScanningFrequencySetting = "Manual Scanning";
    public static final int defaultInterval = 15;
    public static final String defaultBluetoothSetting = "Bluetooth";
    public static final boolean defaultPendingSyncToNetwork = false;
    public static final boolean defaultJoinedNetwork = false;

    public static String scanningFrequencySetting = null;
    public static int interval;
    public static String bluetoothSetting = null;
    public static boolean pendingDataSyncedToNetwork = false;
    public static boolean joinedNetwork = false;

    //only for final commit of settings to memory in case changed
    public static boolean hasSettingsChanged = false;
}

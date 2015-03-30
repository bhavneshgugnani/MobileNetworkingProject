package networking.mobile.mobilenetworkingproject.state;

/**
 * Created by Bhavnesh Gugnani on 3/28/2015.
 */
public class ApplicationState {

    public static String CONTINUOUS_SCANNING_SETTING;
    public static String INTERVAL_SCANNING_SETTING;
    public static String MANUAL_SCANNING_SETTING;

    public static String defaultScanningFrequencySetting = "Continuous Scanning";
    public static int defaultInterval = 60;

    public static String scanningFrequencySetting =  null;
    public static boolean hasScanningFrequencySettingsChanged = false;

    public static int interval;
    public static boolean hasIntervalChanged;
}

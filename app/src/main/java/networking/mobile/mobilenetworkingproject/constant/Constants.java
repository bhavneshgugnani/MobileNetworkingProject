package networking.mobile.mobilenetworkingproject.constant;

import java.util.UUID;

/**
 * Created by Bhavnesh Gugnani on 4/7/2015.
 */
public class Constants {

    public static final String BACKUP_PATH = "/networkbackup";

    // Name for creating server socket
    public static final String APP_NAME = "MobileNetworkingProject";

    // Unique UUID for this application
    public static final UUID APP_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public static final String HANDLER_MSG = "handler_message";
    public static final String HANDLER_MSG_KEY = "handler_message_key";

    public static final boolean DEFAULT_PENDING_SYNC_TO_NETWORK = false;

    public static final int ONE_MINUTE_IN_MILLISECOND = 60000;
}

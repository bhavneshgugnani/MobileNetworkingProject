package networking.mobile.mobilenetworkingproject.broadcastreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import networking.mobile.mobilenetworkingproject.constant.Constants;
import networking.mobile.mobilenetworkingproject.controller.DataSyncController;
import networking.mobile.mobilenetworkingproject.state.ApplicationState;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String ADJUST_SCANNING_INTENT = "mobilenetworking.adjustscanningintent";
    public static final String MANUAL_UPDATE_INTENT = "mobilenetworking.manualupdateintent";

    private Context context = null;
    private DataSyncController syncController = null;
    private AlarmManager aManager = null;

    public ServiceBroadcastReceiver(Context context, DataSyncController syncController, AlarmManager aManager) {
        this.syncController = syncController;
        this.aManager = aManager;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == MANUAL_UPDATE_INTENT){
            syncController.clearAnyPendingSyncToNetwork();
        } else if(action == ADJUST_SCANNING_INTENT){
            //cancel any old interval scanning
            Intent alarmIntent = new Intent();
            alarmIntent.setAction(ApplicationState.MANUAL_SCANNING_SETTING);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            aManager.cancel(pendingIntent);
            //assign new scanning
            if(intent.getAction().equalsIgnoreCase(ApplicationState.MANUAL_SCANNING_SETTING))
                syncController.clearAnyPendingSyncToNetwork();
            else if(intent.getAction().equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)){
                while(ApplicationState.pendingDataSyncedToNetwork)
                    syncController.clearAnyPendingSyncToNetwork();
            } else if(intent.getAction().equalsIgnoreCase(ApplicationState.INTERVAL_SCANNING_SETTING)){
                //start new interval schedule for scanning
                alarmIntent = new Intent();
                alarmIntent.setAction(ApplicationState.MANUAL_SCANNING_SETTING);
                pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                aManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ApplicationState.interval * Constants.ONE_MINUTE_IN_MILLISECOND, pendingIntent);
            }
        }

    }
}

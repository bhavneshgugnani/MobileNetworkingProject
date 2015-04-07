package networking.mobile.mobilenetworkingproject.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import networking.mobile.mobilenetworkingproject.controller.DataSyncController;

public class IntervalScanningBroadcastReceiver extends BroadcastReceiver {
    private DataSyncController syncController = null;

    public IntervalScanningBroadcastReceiver(DataSyncController syncController) {
        this.syncController = syncController;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        syncController.clearAnyPendingSyncToNetwork();
    }
}

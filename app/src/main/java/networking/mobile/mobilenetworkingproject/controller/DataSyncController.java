package networking.mobile.mobilenetworkingproject.controller;

import networking.mobile.mobilenetworkingproject.state.ApplicationState;

/**
 * Created by Bhavnesh Gugnani on 4/6/2015.
 */
public class DataSyncController {
    private boolean pendingDataSyncedToNetwork = true;

    public DataSyncController(){

    }

    public void syncDataToNetwork(){
        pendingDataSyncedToNetwork = true;
        if(ApplicationState.scanningFrequencySetting.equalsIgnoreCase(ApplicationState.CONTINUOUS_SCANNING_SETTING)){
            //update network with new data
            clearAnyPendingSyncToNetwork();
        }
    }

    public void clearAnyPendingSyncToNetwork(){
        if(pendingDataSyncedToNetwork){
            //scan bluetooth devices and sync data with network
            //TODO
        }
    }
}

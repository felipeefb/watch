package Utils;

import android.util.Log;

/**
 * Created by felipeefb on 14/08/15.
 */
public  class DebugUtils {

    private boolean debugModeOn = true;
    private static final String LOG_TAG = "TennisScoutingWear";

    public void debugLog(String message){
        if(debugModeOn){
            Log.d(LOG_TAG, message);
        }
    }
}

package com.sony.tennisscouting;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import Utils.DebugUtils;

/**
 * Created by felipeefb on 14/08/15.
 */
public class TennisScoutingListenerService extends WearableListenerService {
    private static final String WEARABLE_DATA_PATH = "/message_path";
    DebugUtils debug = new DebugUtils();

//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        DataMap dataMap;
//        for (DataEvent event : dataEvents) {
//
//            // Check the data type
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // Check the data path
//                String path = event.getDataItem().getUri().getPath();
//                if (path.equals(WEARABLE_DATA_PATH)) {
//                }
//                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
//                debug.debugLog("DataMap received on watch: " + dataMap);
//            }
//        }
//    }

        @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.v("TennisScouting", "Message path received on watch is: " + messageEvent.getPath());
            Log.v("TennisScouting", "Message received on watch is: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }


}

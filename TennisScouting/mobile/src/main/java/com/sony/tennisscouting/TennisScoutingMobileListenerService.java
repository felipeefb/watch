package com.sony.tennisscouting;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by felipeefb on 14/08/15.
 */
public class TennisScoutingMobileListenerService extends WearableListenerService{
    private static final String WEARABLE_DATA_PATH = "/message_path";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    final Intent dataIntent = new Intent();
                    dataIntent.putExtra("x", dataMap.getFloatArray("XAxis"));
                    dataIntent.putExtra("y", dataMap.getFloatArray("YAxis"));
                    dataIntent.putExtra("Z", dataMap.getFloatArray("ZAxis"));
                    dataIntent.setAction(Intent.ACTION_SEND);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(dataIntent);
                    Log.d("TennisScouting", "DataMap received on device: " + dataMap);
                }
            }
        }
    }

    //    @Override
//    public void onMessageReceived(MessageEvent messageEvent) {
//        if (messageEvent.getPath().equals("/message_path")) {
//            final String message = new String(messageEvent.getData());
//            final Intent messageIntent = new Intent();
//            messageIntent.setAction(Intent.ACTION_SEND);
//            messageIntent.putExtra("message", message);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
//        }
//        else {
//            super.onMessageReceived(messageEvent);
//        }
//    }
}

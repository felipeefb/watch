package com.sony.tennisscouting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

import Utils.DebugUtils;

public class TennisScoutingWearMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    private boolean isConnected;
    private GoogleApiClient mGoogleClient;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DebugUtils debug;
    private long lastUpdate;
    private boolean mCanSendToDevice;
    private List<Float> mPositionX;
    private List<Float> mPositionY;
    private List<Float> mPositionZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastUpdate = System.currentTimeMillis();
        mPositionX = new ArrayList<Float>();
        mPositionY = new ArrayList<Float>();
        mPositionZ = new ArrayList<Float>();
        mCanSendToDevice = false;
        debug = new DebugUtils();
        setContentView(R.layout.activity_tennis_scouting_wear_main);
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    public void onConnected(Bundle bundle) {
        isConnected = true;
        debug.debugLog("connectado com o device");
    }

    @Override
    public void onConnectionSuspended(int i) {
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!connectionResult.isSuccess()) {
            isConnected = false;
        }
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        long curTime = System.currentTimeMillis();
        if ((((curTime - lastUpdate) % 100) == 0) || (mCanSendToDevice)) {

            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.
            final float alpha = (float) 0.8;
            // Isolate the force of gravity with the low-pass filter.
            float[] gravity = new float[3];
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            mPositionX.add(event.values[0] - gravity[0]);
            mPositionY.add(event.values[1] - gravity[1]);
            mPositionZ.add(event.values[2] - gravity[2]);

        }

    }

    private void cloneList(float[] array, List<Float> list) {
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != mGoogleClient && mGoogleClient.isConnected()) {
            mGoogleClient.disconnect();
        }
        super.onStop();
    }

    class SendToDataLayerThread extends Thread {
        private String path;
        private DataMap mDataMap;

        SendToDataLayerThread(String p, DataMap d) {
            path = p;
            mDataMap = d;

        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleClient).await();
            for (Node node : nodes.getNodes()) {
                PutDataMapRequest putDMR = PutDataMapRequest.create(path);
                putDMR.getDataMap().putAll(mDataMap);
                PutDataRequest request = putDMR.asPutDataRequest();
                DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleClient, request).await();
                if (result.getStatus().isSuccess()) {
                    debug.debugLog("dataMap: " + mDataMap + " Sent to: " + node.getDisplayName());
                } else {
                    debug.debugLog("ERROR: FAILED TO SEND DATAMAP");
                }

//                MessageApi.SendMessageResult result = Wearable.MessageApi.s
//                if (result.getStatus().isSuccess()) {
//                    debug.debugLog("Message: {" + message + "} sent to: " + node.getDisplayName());
//                } else {
//                    // Log an error
//                    debug.debugLog("ERROR: failed to send Message");
//                }
            }
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float[] x, y, z;
            if (isConnected) {
                x = new float[mPositionX.size()];
                cloneList(x, mPositionX);
                y = new float[mPositionY.size()];
                cloneList(y, mPositionY);
                z = new float[mPositionZ.size()];
                cloneList(z, mPositionZ);
                debug.debugLog("mandando mensagem, tamanho das listas respectivamente: " + mPositionX.size() + ", " + mPositionY.size() + ", " + ", " + mPositionZ);
                DataMap mDataMap = new DataMap();
                mDataMap.putFloatArray("XAxis", x);
                mDataMap.putFloatArray("YAxis", y);
                mDataMap.putFloatArray("ZAxis", z);
                new SendToDataLayerThread("/message_path", mDataMap).start();
                mPositionX.clear();
                mPositionY.clear();
                mPositionZ.clear();
            }
        }
    }
}

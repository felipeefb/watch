package com.sony.tennisscouting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class TennisScoutingMainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;
    private Button mButtom;
    private GoogleApiClient mGoogleClient;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_scouting_main);
        mTextView = (TextView) findViewById(R.id.hello);
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        isConnected = false;
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mButtom = (Button) findViewById(R.id.buttonAnalyse);
        mButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    new SendToDataLayerThread("/message_path", "sim");
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        isConnected = true;
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
        private String message;

        SendToDataLayerThread(String p, String m) {
            path = p;
            message = m;

        }

        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.d("TennisScouting", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.d("TennisScouting", "ERROR: failed to send Message");
                }
            }
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            float[] x = intent.getFloatArrayExtra("x");
            float[] y = intent.getFloatArrayExtra("y");
            float[] z = intent.getFloatArrayExtra("z");
            Intent chartIntent;
            chartIntent = new Intent(getApplicationContext(), ChartActivity.class);
            chartIntent.putExtra("x", x);
            startActivity(chartIntent);

        }
    }

}

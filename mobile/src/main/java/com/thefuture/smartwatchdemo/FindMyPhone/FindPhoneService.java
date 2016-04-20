package com.thefuture.smartwatchdemo.FindMyPhone;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.thefuture.smartwatchdemo.Utility;

import java.util.concurrent.TimeUnit;

public class FindPhoneService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SmartWatchDemoPhone";

    public static final String PATH_TRUST_WIFI = "/path_trust_wifi";

    public static final String ACTION_BROADCAST_ALARM_STATE_CHANGE = "action_broadcast_alarm_state_change";
    public static final String EXTRA_KEY_ALARM_STATE = "extra.key.ALARM_STATE";

    private static final String ACTION_SEND_MSG_UPDATE_TRUST_WIFI = "action.SEND_MSG_UPDATE_TRUST_WIFI";
    public static final String EXTRA_KEY_MSG_TRUST_WIFI = "extra.key.MSG_TRUST_WIFI";

    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private GoogleApiClient mGoogleApiClient;

    public FindPhoneService() {
        super(FindPhoneService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        Log.d(TAG, "onHandleIntent action :" + intent.getAction());
        if (mGoogleApiClient.isConnected()) {
            // Set the alarm off by default.
            if (intent.getAction().equals(ACTION_BROADCAST_ALARM_STATE_CHANGE)) {
                final boolean alarmOn = intent.getBooleanExtra(EXTRA_KEY_ALARM_STATE, false);
                Log.d(TAG, "broadcast alarm status : " + alarmOn);
                Utility.broadcastAlarmStatus(mGoogleApiClient, alarmOn);
            } else if (intent.getAction().equals(ACTION_SEND_MSG_UPDATE_TRUST_WIFI)) {
                // transfer updated trust wifi list to wear app
                final String msgData = intent.getStringExtra(EXTRA_KEY_MSG_TRUST_WIFI);
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Log.d(TAG, "connected node:" + node.getDisplayName());
                    final Node tmpNode = node;
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(mGoogleApiClient, tmpNode.getId(), PATH_TRUST_WIFI, msgData.getBytes());
                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                Log.v(TAG, "Message: {" + msgData + "} sent to: " + tmpNode.getDisplayName());
                            } else {
                                Log.v(TAG, "ERROR: failed to send Message");
                            }
                        }
                    });
                }
            }

        } else {
            Log.e(TAG, "Failed to toggle alarm on phone - Client disconnected from Google Play "
                    + "Services");
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    public static void broadcastAlarmStateChange(Context ctx, boolean alarmOn) {
        Intent intent = new Intent(ctx, FindPhoneService.class);
        intent.setAction(FindPhoneService.ACTION_BROADCAST_ALARM_STATE_CHANGE);
        intent.putExtra(EXTRA_KEY_ALARM_STATE, alarmOn);
        ctx.startService(intent);
    }

    public static void sendMsgToUpdateTrustWifi(Context ctx, String msgData) {
        Intent intent = new Intent(ctx, FindPhoneService.class);
        intent.setAction(ACTION_SEND_MSG_UPDATE_TRUST_WIFI);
        intent.putExtra(EXTRA_KEY_MSG_TRUST_WIFI, msgData);
        ctx.startService(intent);
    }
}

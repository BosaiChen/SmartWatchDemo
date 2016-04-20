package com.thefuture.smartwatchdemo;

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

import java.util.concurrent.TimeUnit;

/**
 * Creates a sound on the paired phone to find it.
 */
public class FindPhoneService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SmartWatchDemoWear";

    public static final String PATH_ALARM_STATE = "/path_alarm_state";

    public static final String ACTION_GET_ALARM_STATE = "action_get_alarm_state";
    public static final String EXTRA_KEY_ALARM_STATE = "extra.key.ALARM_STATE";
    public static final String ACTION_BROADCAST_ALARM_STATE_CHANGE = "action_broadcast_alarm_state_change";

    public static final String ACTION_SEND_MSG_UPDATE_ALARM_STATE = "action.SEND_MSG_UPDATE_ALARM_STATE";
    public static final String EXTRA_KEY_MSG_ALARM_STATE = "extra.key.MSG_ALARM_STATE";

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
        Log.d(TAG, "FindPhoneService.onHandleIntent receive action:" + intent.getAction());
        if (mGoogleApiClient.isConnected()) {
            // Set the alarm off by default.
            if (intent.getAction().equals(ACTION_BROADCAST_ALARM_STATE_CHANGE)) {
                final boolean alarmOn = intent.getBooleanExtra(EXTRA_KEY_ALARM_STATE, false);
                Utility.broadcastAlarmStatus(mGoogleApiClient, alarmOn);
            } else if (intent.getAction().equals(ACTION_GET_ALARM_STATE)) {
                Settings.setAlarmOn(getApplicationContext(), Utility.getAlarmStatus(mGoogleApiClient));
            } else if (intent.getAction().equals(ACTION_SEND_MSG_UPDATE_ALARM_STATE)) {
                final String msgData = intent.getStringExtra(EXTRA_KEY_MSG_ALARM_STATE);
                sendMessage(PATH_ALARM_STATE, msgData);
            }
        } else {
            Log.e(TAG, "Client disconnected from Google Play Services");
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

    @Deprecated
    public static void broadcastAlarmStateChange(Context ctx, boolean alarmOn) {
        Intent intent = new Intent(ctx, FindPhoneService.class);
        intent.setAction(FindPhoneService.ACTION_BROADCAST_ALARM_STATE_CHANGE);
        intent.putExtra(EXTRA_KEY_ALARM_STATE, alarmOn);
        ctx.startService(intent);
    }

    public static void getAlarmStateFromGA(Context ctx) {
        Intent intent = new Intent(ctx, FindPhoneService.class);
        intent.setAction(FindPhoneService.ACTION_GET_ALARM_STATE);
        ctx.startService(intent);
    }

    public static void sendMsgToUpdateAlarmState(Context ctx, boolean alarmOn) {
        Intent intent = new Intent(ctx, FindPhoneService.class);
        intent.setAction(ACTION_SEND_MSG_UPDATE_ALARM_STATE);
        intent.putExtra(EXTRA_KEY_MSG_ALARM_STATE, String.valueOf(alarmOn));
        ctx.startService(intent);
    }

    private void sendMessage(String path, final String msgData) {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            Log.d(TAG, "connected node:" + node.getDisplayName());
            final Node tmpNode = node;
            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(mGoogleApiClient, tmpNode.getId(), path, msgData.getBytes());
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
}

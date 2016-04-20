package com.thefuture.smartwatchdemo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Creates a sound on the paired phone to find it.
 */
public class FindPhoneService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SmartWatchDemoWear";

    public static final String ACTION_GET_ALARM_STATE = "action_get_alarm_state";
    public static final String EXTRA_KEY_ALARM_STATE = "extra.key.ALARM_STATE";
    public static final String ACTION_BROADCAST_ALARM_STATE_CHANGE = "action_broadcast_alarm_state_change";

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
        Log.d(TAG, "FindPhoneService.onHandleIntent");
        if (mGoogleApiClient.isConnected()) {
            // Set the alarm off by default.
            if (intent.getAction().equals(ACTION_BROADCAST_ALARM_STATE_CHANGE)) {
                final boolean alarmOn = intent.getBooleanExtra(EXTRA_KEY_ALARM_STATE, false);
                Utility.broadcastAlarmStatus(mGoogleApiClient, alarmOn);
            } else if (intent.getAction().equals(ACTION_GET_ALARM_STATE)) {
                Settings.setAlarmOn(getApplicationContext(), Utility.getAlarmStatus(mGoogleApiClient));
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
}

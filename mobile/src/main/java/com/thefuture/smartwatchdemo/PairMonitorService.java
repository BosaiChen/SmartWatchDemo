package com.thefuture.smartwatchdemo;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.thefuture.smartwatchdemo.FindMyPhone.FindMyPhoneAlarmActivity;
import com.thefuture.smartwatchdemo.FindMyPhone.FindPhoneService;
import com.thefuture.smartwatchdemo.trustwifi.TrustWifiDbHelper;

import java.util.List;

public class PairMonitorService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{
    private static final String TAG = "SmartWatchDemoPhone";
    private static final String FIELD_ALARM_ON = "alarm_on";
    private static final String CAPABILITY_TRUST_WIFI = "capability_trust_wifi";

    private AudioManager mAudioManager;
    private static int mOrigVolume;
    private int mMaxVolume;
    private Uri mAlarmSound;
    private MediaPlayer mMediaPlayer;

    private GoogleApiClient mGoogleApiClient;

    CapabilityApi.CapabilityListener capabilityListener =
            new CapabilityApi.CapabilityListener() {
                @Override
                public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                    Log.d(TAG, "onCapabilityChanged");
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mOrigVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        mAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                CAPABILITY_TRUST_WIFI
        );
    }

    @Override
    public void onDestroy() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mOrigVolume, 0);
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (DataEvent.TYPE_DELETED == event.getType()) {
                Log.d(TAG, event + " deleted");
            } else if (DataEvent.TYPE_CHANGED == event.getType()) {
                Boolean alarmOn = DataMap.fromByteArray(event.getDataItem().getData()).get(FIELD_ALARM_ON);
                Log.d(TAG, "alarm on ? " + alarmOn);
                if (alarmOn) {
                    onAlarmTriggered();
                } else {
                    Utility.cancelAlarm(getApplicationContext());
                }
            }
        }
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        super.onConnectedNodes(connectedNodes);
        Log.d(TAG, "onConnectedNodes()");
        if (mGoogleApiClient.isConnected()) {
            syncTrustWifiDB();
        } else if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        syncTrustWifiDB();
    }

    private void syncTrustWifiDB() {
        Log.d(TAG, "syncTrustWifiDB()");
        FindPhoneService.sendMsgToUpdateTrustWifi(getApplicationContext(), TrustWifiDbHelper.getAllWifisInJSON(getApplicationContext()));
    }

    private void onAlarmTriggered() {
        Intent intent = new Intent(getApplicationContext(), FindMyPhoneAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD + WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        startActivity(intent);

        Utility.startAlarm(getApplicationContext());
    }

}

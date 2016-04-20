package com.thefuture.smartwatchdemo;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class Utility {
    private static Ringtone mRingTone;
    private static final String TAG = "SmartWatchDemoWear";
    private static final String FIELD_ALARM_ON = "alarm_on";
    private static final String PATH_SOUND_ALARM = "/sound_alarm_2";

    /**
     * If fail to get alarm status, return false
     */
    public static boolean getAlarmStatus(GoogleApiClient gaClient) {
        boolean alarmOn = false;
        DataItemBuffer result = Wearable.DataApi.getDataItems(gaClient).await();
        try {
            if (result.getStatus().isSuccess()) {
                if (result.getCount() == 1) {
                    alarmOn = DataMap.fromByteArray(result.get(0).getData())
                            .getBoolean(FIELD_ALARM_ON, false);
                } else {
                    Log.e(TAG, "Unexpected number of DataItems found.\n"
                            + "\tExpected: 1\n"
                            + "\tActual: " + result.getCount());
                }
            } else {
                Log.d(TAG, "onHandleIntent: failed to get current alarm state");
            }
        } finally {
            result.release();
        }

        return alarmOn;
    }

    public static void broadcastAlarmStatus(GoogleApiClient gaClient, boolean alarmOn) {
        // Use alarmOn boolean to update the DataItem - phone will respond accordingly
        // when it receives the change.
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_SOUND_ALARM);
        putDataMapRequest.getDataMap().putBoolean(FIELD_ALARM_ON, alarmOn);
        putDataMapRequest.setUrgent();
        Wearable.DataApi.putDataItem(gaClient, putDataMapRequest.asPutDataRequest())
                .await();
    }


    public static void startAlarm(Context context) {
        try {
            Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (mRingTone == null) {
                mRingTone = RingtoneManager.getRingtone(context, alarm);
            }
            if (!mRingTone.isPlaying()) {
                mRingTone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelAlarm(Context context) {
        if (mRingTone != null && mRingTone.isPlaying()) {
            mRingTone.stop();
        }
    }

    public static GoogleApiClient getGAClientInstanceForWearable(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }
}

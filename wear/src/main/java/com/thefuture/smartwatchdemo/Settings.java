package com.thefuture.smartwatchdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class Settings {
    private static final String PREFERENCE_WEAR_APP = "preference.WEAR_APP";
    private static final String KEY_PEER_DISCONNECTED = "key.PEER_DISCONNECTED";
    private static final String KEY_LAST_CONNECTED_PEER_NAME = "key.LAST_CONNECTED_PEER_NAME";
    public static final String KEY_ALARM_ON = "key.ALARM_ON";

    public static void setPeerDisconnected(Context ctx, boolean disconnected) {
        getSharedPreference(ctx).edit().putBoolean(KEY_PEER_DISCONNECTED, disconnected).apply();
    }

    public static boolean getPeerDisconnected(Context ctx) {
        return getSharedPreference(ctx).getBoolean(KEY_PEER_DISCONNECTED, true);
    }

    public static void setLastConnectedPeerName(Context ctx, String name) {
        getSharedPreference(ctx).edit().putString(KEY_LAST_CONNECTED_PEER_NAME, name).apply();
    }

    public static String getLastConnectedPeerName(Context ctx) {
        return getSharedPreference(ctx).getString(KEY_LAST_CONNECTED_PEER_NAME, "");
    }

    public static SharedPreferences getSharedPreference(@NonNull Context ctx) {
        return ctx.getSharedPreferences(PREFERENCE_WEAR_APP, Context.MODE_PRIVATE);
    }

    public static void setAlarmOn(Context ctx, boolean alarmOn) {
        getSharedPreference(ctx).edit().putBoolean(KEY_ALARM_ON, alarmOn).apply();
    }

    /**
     * Alarm is OFF by default
     */
    public static boolean getAlarmOn(Context ctx) {
        return getSharedPreference(ctx).getBoolean(KEY_ALARM_ON, false);
    }
}

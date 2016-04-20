package com.thefuture.smartwatchdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class Settings {
    private static final String PREFERENCE_APP = "preference.APP";
    private static final String KEY_ALARM_ENABLE = "pref.key.ALARM_ENABLE";
    private static final String KEY_WEAR_RECEIVE_NOTIFICATION = "pref.key.WEAR_RECEIVE_NOTIFICATION";

    public void setAlarmEnable(Context ctx, final boolean enable) {
        getSharedPreference(ctx).edit().putBoolean(KEY_ALARM_ENABLE, enable);
    }

    /**
     * Alarm is enabled by default
     * @return true by default
     */
    public boolean getAlarmEnable(@NonNull Context ctx) {
        return getSharedPreference(ctx).getBoolean(KEY_ALARM_ENABLE, true);
    }

    public void setWearReceiveNotification(@NonNull Context ctx, boolean receive) {
        getSharedPreference(ctx).edit().putBoolean(KEY_WEAR_RECEIVE_NOTIFICATION, receive);
    }

    /**
     * By default, wear device will receive notification from phone
     * @return true by default
     */
    public boolean isWearReceiveNotification(@NonNull Context ctx) {
        return getSharedPreference(ctx).getBoolean(KEY_WEAR_RECEIVE_NOTIFICATION, true);
    }

    private SharedPreferences getSharedPreference(@NonNull Context ctx) {
        return ctx.getSharedPreferences(PREFERENCE_APP, Context.MODE_PRIVATE);
    }

}

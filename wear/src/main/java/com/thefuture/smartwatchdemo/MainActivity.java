package com.thefuture.smartwatchdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.ActionPage;
import android.support.wearable.view.WatchViewStub;
import android.view.View;

public class MainActivity extends Activity {
    private ActionPage mAlarmAction;
    private boolean mAlarmOn;

    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Settings.KEY_ALARM_ON)) {
                mAlarmOn = Settings.getAlarmOn(getApplicationContext());
                updateAlarmAction(mAlarmOn);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAlarmOn = Settings.getAlarmOn(this);
        Settings.getSharedPreference(this).registerOnSharedPreferenceChangeListener(mListener);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mAlarmAction = (ActionPage) stub.findViewById(R.id.alarm_action);
                updateAlarmAction(mAlarmOn);

                mAlarmAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlarmOn = !mAlarmOn;
                        FindPhoneService.sendMsgToUpdateAlarmState(getApplicationContext(), mAlarmOn);
                        updateAlarmAction(mAlarmOn);
                        Settings.setAlarmOn(getApplicationContext(), mAlarmOn);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAlarmOn = Settings.getAlarmOn(this);
        FindPhoneService.getAlarmStateFromGA(this);
    }

    @Override
    protected void onDestroy() {
        Settings.getSharedPreference(this).unregisterOnSharedPreferenceChangeListener(mListener);
        super.onDestroy();
    }

    private void updateAlarmAction(boolean alarmOn) {
        String actionText = alarmOn ? getString(R.string.tap_to_stop) : getString(R.string.tap_to_find);
        int actionImg = alarmOn ? R.drawable.ic_alarm_off_white_24dp : R.drawable.ic_alarm_white_24dp;

        mAlarmAction.setText(actionText);
        mAlarmAction.setImageResource(actionImg);
    }
}

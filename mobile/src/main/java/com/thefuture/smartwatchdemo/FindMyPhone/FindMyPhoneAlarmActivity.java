package com.thefuture.smartwatchdemo.FindMyPhone;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.thefuture.smartwatchdemo.R;
import com.thefuture.smartwatchdemo.Utility;

public class FindMyPhoneAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_my_phone_alarm);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Button stopAlarmBtn = (Button) findViewById(R.id.btn_stop_alarm);
        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Utility.cancelAlarm(getApplicationContext());
        FindPhoneService.sendMsgToUpdateAlarmState(getApplicationContext(), false);
        super.onDestroy();
    }
}

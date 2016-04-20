package com.thefuture.smartwatchdemo.trustwifi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.thefuture.smartwatchdemo.R;

public class TrustWifiActivity extends AppCompatActivity implements ScanWifiFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trust_wifi);
    }

    @Override
    public void onListFragmentInteraction(WifiInfoItem item) {
//        TrustWifiDbHelper.updateWifi(this, item.bssID, );
    }
}

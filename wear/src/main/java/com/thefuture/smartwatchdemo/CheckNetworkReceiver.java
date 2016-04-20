package com.thefuture.smartwatchdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class CheckNetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "SmartWatchDemoWear";
    private static final String BURGUNDY_MAC_ADDR = "a0:39:f7:43:1a:fe";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "inside CheckNetworkReceiver");
        boolean trustedWifi = false;
        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.getConnectionInfo().getMacAddress().equals(BURGUNDY_MAC_ADDR)) {
                trustedWifi = true;
                Log.d(TAG, "trusted wifi");
            }
        }
        if (Settings.getPeerDisconnected(context) && !trustedWifi) {
            Log.d(TAG, "notify device disconnected");
            Notifier.notifyDeviceDisconnected(context, Settings.getLastConnectedPeerName(context));
        }
    }
}

package com.thefuture.smartwatchdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

@Deprecated
public class CheckNetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "SmartWatchDemoWear";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "inside CheckNetworkReceiver");
        boolean trustedWifi = false;
        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final String bssid = wifiManager.getConnectionInfo().getBSSID();
            WifiInfoItem wifi = TrustWifiDbHelper.getWifi(context, bssid);
            if (wifi != null && wifi.trust) {
                Log.d(TAG, "wifi " + wifi.displayName + " is trusted");
                trustedWifi = true;
            }
        }
        if (Settings.getPeerDisconnected(context) && !trustedWifi) {
            Log.d(TAG, "notify device disconnected");
            Notifier.notifyDeviceDisconnected(context, Settings.getLastConnectedPeerName(context));
        }
    }
}

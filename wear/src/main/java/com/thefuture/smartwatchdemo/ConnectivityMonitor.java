package com.thefuture.smartwatchdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class ConnectivityMonitor extends BroadcastReceiver {
    private static final String TAG = "SmartWatchDemoWear";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            return;
        }

        Log.d(TAG, "ConnectivityMonitor receive action: " + action);

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            final boolean noConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (noConnection) {
                Log.d(TAG, "no connection, phone may be insecure, show notification");
//                Notifier.notifyDeviceDisconnected(context, "your device");
//                return;
            }

            final boolean failoverConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            if (failoverConnection) {
                logInfo(context, "failoverConnection");
            }

            final int networkType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
            logInfo(context, "network type: " + networkType);
            if (networkType == ConnectivityManager.TYPE_WIFI) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final String bssid = wifiManager.getConnectionInfo().getBSSID();
                WifiInfoItem wifi = TrustWifiDbHelper.getWifi(context, bssid);
                if (wifi == null || !wifi.trust) {
                    Log.d(TAG, "wifi is not trusted");
                    Notifier.notifyDeviceDisconnected(context, "your device");
                    return;
                }

                Log.d(TAG, "wifi " + wifi.displayName + " is trusted, not show notification");
//                logInfo(context, "wifi ssid:" + wifiInfo.getSSID() + "|| Mac: " + wifiInfo.getMacAddress());
            }
        }

        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            logInfo(context, networkInfo.getTypeName());
        }


    }

    /*private String getUnsecureWiFiIdentifier(final Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final List<ScanResult> networkList = wifiManager.getScanResults();
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (networkList != null) {
            final String currentSSID = getSSIDFromWifiInfo(wifiInfo);
            final String currentBSSID = getBSSIDFromWifiInfo(wifiInfo);
            if (currentSSID != null && currentBSSID != null) {
                for (final ScanResult network : networkList) {
                    if (currentSSID.equals(network.SSID) && currentBSSID.equals(network.BSSID)) {
                        final String capabilities = network.capabilities;
                        if (capabilities != null && !capabilities.contains(SECURE_WIFI_PROTOCOL)) {
                            return currentSSID;
                        }
                    }
                }
            }
        }
        return null;
    }*/

    private String getSSIDFromWifiInfo(@NonNull final WifiInfo wifiInfo) {
        final String currentSSID = wifiInfo.getSSID();
        if (currentSSID != null && currentSSID.startsWith("\"") && currentSSID.endsWith("\"")) {
            return currentSSID.substring(1, currentSSID.length() - 1);
        }
        return currentSSID;
    }

    private String getBSSIDFromWifiInfo(@NonNull final WifiInfo wifiInfo) {
        return wifiInfo.getBSSID();
    }

    private void logInfo(Context ctx, String info) {
//        Toast.makeText(ctx, info, Toast.LENGTH_SHORT).show();
        Log.d(TAG, info);
    }
}

package com.thefuture.smartwatchdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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
//                Intent alarmIntent = new Intent(context, CheckNetworkReceiver.class);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//                am.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 15000, pendingIntent);
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
                if (bssid != null) {
                    WifiInfoItem wifi = TrustWifiDbHelper.getWifi(context, bssid);
                    if (wifi == null || !wifi.trust) {
                        Log.d(TAG, "wifi is not trusted");
                        Notifier.notifyDeviceDisconnected(context, "your device");
                        return;
                    }

                    Log.d(TAG, "wifi " + wifi.displayName + " is trusted, not show notification");
                }
            }
        }

        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        Log.d(TAG, "networkinfo == null?" + (networkInfo==null));
        if (networkInfo != null) {
            logInfo(context, networkInfo.getTypeName());
        }


    }

    private void logInfo(Context ctx, String info) {
//        Toast.makeText(ctx, info, Toast.LENGTH_SHORT).show();
        Log.d(TAG, info);
    }
}

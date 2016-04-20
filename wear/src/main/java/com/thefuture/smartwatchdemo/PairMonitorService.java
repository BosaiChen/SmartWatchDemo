package com.thefuture.smartwatchdemo;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.Set;

public class PairMonitorService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{
    private static final String TAG = "SmartWatchDemoWear";

    private static final String FIND_ME_CAPABILITY_NAME = "find_me";
    private static final String FIELD_ALARM_ON = "alarm_on";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        /*FeatureInfo[] features = getPackageManager().getSystemAvailableFeatures();
        for (FeatureInfo info : features) {
            //&& info.name.equalsIgnoreCase("android.hardware.wifi.direct")
            if (info != null && info.name != null) {
                Log.i(TAG, info.name);
            }
        }*/
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.d(TAG, "onPeerConnected: " + peer);
        Settings.setLastConnectedPeerName(getApplicationContext(), peer.getDisplayName());
        Settings.setPeerDisconnected(getApplicationContext(), false);
        Notifier.cancelNotification(getApplicationContext(), Notifier.NOTIFICATION_ID_DEVICE_DISCONNECTED);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.d(TAG, "onPeerDisconnected: " + peer);
        Settings.setPeerDisconnected(getApplicationContext(), true);
        Notifier.notifyDeviceDisconnected(getApplicationContext(), peer.getDisplayName());
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        super.onConnectedNodes(connectedNodes);
        Log.d(TAG, "onConnectedNodes()");
        if (mGoogleApiClient.isConnected()) {
            setOrUpdateNotification();
        } else if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents);

        for (DataEvent event : dataEvents) {
            if (DataEvent.TYPE_DELETED == event.getType()) {
                Log.d(TAG, event + " deleted");
            } else if (DataEvent.TYPE_CHANGED == event.getType()) {
                Boolean alarmOn = DataMap.fromByteArray(event.getDataItem().getData()).get(FIELD_ALARM_ON);
                Log.d(TAG, "alarm on ? " + alarmOn);
                Settings.setAlarmOn(getApplicationContext(), alarmOn);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "receive message from path " + messageEvent.getPath());
        if (messageEvent.getPath().equals("/path_trust_wifi")) {
            TrustWifiDbHelper.parseWifisJSONAndSaveToDB(getApplicationContext(), new String(messageEvent.getData()));
        } else if (messageEvent.getPath().equals("/path_alarm_state")) {
            final boolean alarmOn = Boolean.valueOf(new String(messageEvent.getData()));
            Settings.setAlarmOn(getApplicationContext(), alarmOn);
            Log.d(TAG, "message says alarm on ? " + alarmOn);
        }
    }

    private void setOrUpdateNotification() {
        Wearable.CapabilityApi.getCapability(
                mGoogleApiClient, FIND_ME_CAPABILITY_NAME,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(
                new ResultCallback<CapabilityApi.GetCapabilityResult>() {
                    @Override
                    public void onResult(CapabilityApi.GetCapabilityResult result) {
                        Log.d(TAG, "get capability of find_me success? " + result.getStatus().isSuccess());
                        if (result.getStatus().isSuccess()) {
                            updateFindMeCapability(result.getCapability());
                        } else {
                            Log.d(TAG,
                                    "setOrUpdateNotification() Failed to get capabilities, "
                                            + "status: "
                                            + result.getStatus().getStatusMessage());
                        }
                    }
                });
    }

    private void updateFindMeCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        if (connectedNodes.isEmpty()) {
            Notifier.notifyDeviceDisconnected(getApplicationContext(), "Your device ");
        } else {
            for (Node node : connectedNodes) {
                // we are only considering those nodes that are directly connected
                if (node.isNearby()) {
                    Notifier.cancelNotification(getApplicationContext(), Notifier.NOTIFICATION_ID_DEVICE_DISCONNECTED);
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        setOrUpdateNotification();
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }
}

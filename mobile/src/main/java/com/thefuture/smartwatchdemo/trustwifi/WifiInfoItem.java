package com.thefuture.smartwatchdemo.trustwifi;

public class WifiInfoItem {
    public String displayName;
    public String bssID;
    public boolean trust;

    public WifiInfoItem() {
        this("", "", false);
    }

    public WifiInfoItem(String displayName, String bssID, boolean trust) {
        this.displayName = displayName;
        this.bssID = bssID;
        this.trust = trust;
    }
}

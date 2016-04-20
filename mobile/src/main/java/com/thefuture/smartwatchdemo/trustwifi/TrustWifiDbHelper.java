package com.thefuture.smartwatchdemo.trustwifi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TrustWifiDbHelper {
    private static final String TAG = "SmartWatchDemoWear";

    public static void updateWifi(Context ctx, String bssID, boolean isTrust) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLUMN_IS_TRUST, isTrust ? 1 : 0);

        SQLiteDatabase db = null;

        try {
            db = new DBHelper(ctx).getWritableDatabase();
            long rowId = db.update(DBHelper.TABLE_NAME, cv, DBHelper.COLUMN_WIFI_BSSID + "=?", new String[]{bssID});
            if (rowId <= 0) {
                cv.put(DBHelper.COLUMN_WIFI_BSSID, bssID);
                cv.put(DBHelper.COLUMN_WIFI_NAME, "");
                db.insertWithOnConflict(DBHelper.TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error to update app.", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public static void deleteWifi(Context ctx, String bssid) {
        SQLiteDatabase db = null;
        try {
            db = new DBHelper(ctx).getWritableDatabase();
            db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_WIFI_BSSID + "=?", new String[]{bssid});
            Log.d(TAG, "deleted wifi bssid - " + bssid);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error to delete wifi from db.");
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Query wifi of specific BSSID.
     * Return null if no result found.
     */
    public static WifiInfoItem getWifi(Context ctx, String bssID) {
        SQLiteDatabase db = null;
        Cursor c = null;

        try {
            db = new DBHelper(ctx).getReadableDatabase();
            c = db.query(DBHelper.TABLE_NAME, null, DBHelper.COLUMN_WIFI_BSSID + "=?", new String[]{bssID}, null, null, null);
            if (c != null && c.moveToFirst()) {
                WifiInfoItem wifiInfo = new WifiInfoItem();
                wifiInfo.bssID = c.getString(c.getColumnIndex(DBHelper.COLUMN_WIFI_BSSID));
                wifiInfo.displayName = c.getString(c.getColumnIndex(DBHelper.COLUMN_WIFI_NAME));
                wifiInfo.trust = c.getInt(c.getColumnIndex(DBHelper.COLUMN_IS_TRUST)) > 0;
                return wifiInfo;
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error to get wifi info.");
        } finally {
            if (c != null) {
                c.close();
            }

            if (db != null) {
                db.close();
            }
        }

        return null;
    }

    public static HashMap<String, WifiInfoItem> getAllScannedWifis(Context ctx) {
        HashMap<String, WifiInfoItem> scannedWifiList = new HashMap<>();
        final WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        StringBuilder sb = new StringBuilder();
        sb.append("Scanned wifi: ");
        for (ScanResult scanResult : scanResults) {
            sb.append(scanResult.SSID + " | ");
            if (TextUtils.isEmpty(scanResult.SSID)) {
                // no need to show wifi with no display name
                continue;
            }

            WifiInfoItem wifiInfoItem = getWifi(ctx, scanResult.BSSID);
            if (wifiInfoItem != null) {
                // wifi record found in DB
                wifiInfoItem.displayName = scanResult.SSID;
                scannedWifiList.put(scanResult.BSSID, wifiInfoItem);
            } else {
                scannedWifiList.put(scanResult.BSSID, new WifiInfoItem(scanResult.SSID, scanResult.BSSID, false));
            }
        }

        Log.d(TAG, sb.toString());

        return scannedWifiList;
    }

    public static List<WifiInfoItem> getAllScannedWifisInList(Context ctx) {
        HashMap<String, WifiInfoItem> wifisMap = getAllScannedWifis(ctx);
        List<WifiInfoItem> returnWifiList = new ArrayList<>();
        for (String key : wifisMap.keySet()) {
            returnWifiList.add(wifisMap.get(key));
        }
        Collections.sort(returnWifiList, new Comparator<WifiInfoItem>() {
            @Override
            public int compare(WifiInfoItem lhs, WifiInfoItem rhs) {
                return lhs.displayName.compareToIgnoreCase(rhs.displayName);
            }
        });
        return returnWifiList;
    }

    /**
     * Return null if no wifi records found in DB
     */
    public static String getAllWifisInJSON(Context ctx) {
        return convertWifisToJSON(ctx, getAllScannedWifisInList(ctx));
    }

    public static String convertWifisToJSON(Context ctx, List<WifiInfoItem> wifiInfoItems) {
        try {
            JSONArray wifisArray = new JSONArray();
            for (WifiInfoItem wifi : wifiInfoItems) {
                JSONObject wifiJSON = new JSONObject();
                wifiJSON.put("ssid", wifi.displayName);
                wifiJSON.put("bssid", wifi.bssID);
                wifiJSON.put("trust", wifi.trust);
                wifisArray.put(wifiJSON);
            }

            if (wifisArray.length() > 0) {
                JSONObject wifiListJSON = new JSONObject();
                wifiListJSON.put("wifi_list", wifisArray);
                return wifiListJSON.toString();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    private static class DBHelper extends SQLiteOpenHelper {

        static final String TABLE_NAME = "TrustWifi";
        static final String COLUMN_WIFI_BSSID = "wifiBSSID";
        static final String COLUMN_WIFI_NAME = "wifiName";
        static final String COLUMN_IS_TRUST = "isTrust";

        private static final String DATABASE_NAME = "TrustWifi.db";
        private static final String CREATE_TABLE = "CREATE TABLE TrustWifi (wifiBSSID TEXT PRIMARY KEY, wifiName TEXT, isTrust INTEGER)";
        private static final int DATABASE_VERSION = 1;

        DBHelper(Context context) {
            super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(CREATE_TABLE);
        }
    }
}

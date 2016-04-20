package com.thefuture.smartwatchdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrustWifiDbHelper {
    private static final String TAG = "SmartWatchDemoWear";

    public static void updateWifi(Context ctx, String bssID, String ssid, boolean isTrust) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLUMN_IS_TRUST, isTrust ? 1 : 0);

        SQLiteDatabase db = null;

        try {
            db = new DBHelper(ctx).getWritableDatabase();
            long rowId = db.update(DBHelper.TABLE_NAME, cv, DBHelper.COLUMN_WIFI_BSSID + "=?", new String[]{bssID});
            if (rowId <= 0) {
                cv.put(DBHelper.COLUMN_WIFI_BSSID, bssID);
                cv.put(DBHelper.COLUMN_WIFI_NAME, ssid);
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

    public static void parseWifisJSONAndSaveToDB(Context ctx, String wifisJSON) {
        try {
            StringBuilder sb = new StringBuilder();
            JSONArray wifisArray = (JSONArray)new JSONObject(wifisJSON).get("wifi_list");
            for (int i=0; i < wifisArray.length(); i++) {
                JSONObject wifi = (JSONObject)wifisArray.get(i);
                final String ssid = wifi.getString("ssid");
                final String bssid = wifi.getString("bssid");
                final boolean trust = wifi.getBoolean("trust");
                sb.append("{" + ssid + "," + bssid + "," + trust + "}");
                updateWifi(ctx, bssid, ssid, trust);
            }
            Log.d(TAG, "parsed wifi JSON string: " + sb.toString());
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
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

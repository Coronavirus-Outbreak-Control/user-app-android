package com.example.coronavirusherdimmunity.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.coronavirusherdimmunity.enums.Distance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class StorageManager extends SQLiteOpenHelper {

    // https://developer.android.com/training/data-storage/sqlite#java

    public static class BeaconEntry implements BaseColumns {
        public static final String TABLE_NAME = "Beacons";

        public static final String COLUMN_NAME_IDENTIFIER = "identifier";
        public static final String COLUMN_NAME_RSSI = "rssi";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";

        public static final String COUNT_BEACONS = "countBeacons";

        public static final String INDEX_NAME_IDENTIFIER = "identifier_index";
        public static final String INDEX_NAME_TIMESTAMP = "timestamp_index";
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + BeaconEntry.TABLE_NAME + " (" +
                    BeaconEntry._ID + " INTEGER PRIMARY KEY," +
                    BeaconEntry.COLUMN_NAME_IDENTIFIER + " INTEGER," +
                    BeaconEntry.COLUMN_NAME_RSSI + " INTEGER," +
                    BeaconEntry.COLUMN_NAME_DISTANCE + " INTEGER DEFAULT 0,"+
                    BeaconEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    BeaconEntry.COLUMN_NAME_X + " REAL DEFAULT 0," +
                    BeaconEntry.COLUMN_NAME_Y + " REAL DEFAULT 0)";

    private static final  String SQL_CREATE_IDENTIFIER_INDEX =
            "CREATE INDEX " + BeaconEntry.INDEX_NAME_IDENTIFIER + " ON " +
                    BeaconEntry.TABLE_NAME + " (" + BeaconEntry.COLUMN_NAME_IDENTIFIER + ")";

    private static final  String SQL_CREATE_TIMESTAMP_INDEX =
            "CREATE INDEX " + BeaconEntry.INDEX_NAME_TIMESTAMP + " ON " +
                    BeaconEntry.TABLE_NAME + " (" + BeaconEntry.COLUMN_NAME_TIMESTAMP + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BeaconEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "StorageManager.db";

    public StorageManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_IDENTIFIER_INDEX);
        db.execSQL(SQL_CREATE_TIMESTAMP_INDEX);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertBeacon(BeaconDto beacon){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BeaconEntry.COLUMN_NAME_IDENTIFIER, beacon.identifier);
        values.put(BeaconEntry.COLUMN_NAME_RSSI, beacon.rssi);
        values.put(BeaconEntry.COLUMN_NAME_TIMESTAMP, beacon.timestmp);
        values.put(BeaconEntry.COLUMN_NAME_DISTANCE, beacon.distance.toInt());
        values.put(BeaconEntry.COLUMN_NAME_X, beacon.x);
        values.put(BeaconEntry.COLUMN_NAME_Y, beacon.y);

        long newRowId = db.insert(BeaconEntry.TABLE_NAME, null, values);

        this.close();
    }

    public List<BeaconDto> readBeaconsToday(){
        return this.readBeacons(Helper.getToday());
    }

    public List<BeaconDto> readBeacons(Date timestamp){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
            BeaconEntry._ID,
            BeaconEntry.COLUMN_NAME_IDENTIFIER,
            BeaconEntry.COLUMN_NAME_RSSI,
            BeaconEntry.COLUMN_NAME_TIMESTAMP,
            BeaconEntry.COLUMN_NAME_DISTANCE,
            BeaconEntry.COLUMN_NAME_X,
            BeaconEntry.COLUMN_NAME_Y
        };

        String selection = BeaconEntry.COLUMN_NAME_TIMESTAMP + " >= ?";
        String[] selectionArgs = { Long.toString(timestamp.getTime() / 1000) };

        Cursor cursor = db.query(
                BeaconEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BeaconEntry.COLUMN_NAME_IDENTIFIER + ", " + BeaconEntry.COLUMN_NAME_TIMESTAMP       // The sort order
        );

        List<BeaconDto> beacons = new ArrayList<>();
        while(cursor.moveToNext()) {
            BeaconDto beacon = new BeaconDto(
                    cursor.getLong(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_IDENTIFIER)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_RSSI)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_TIMESTAMP)),
                    Distance.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_DISTANCE))),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_X)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(BeaconEntry.COLUMN_NAME_Y))
            );

            beacons.add(beacon);
        }
        cursor.close();

        return beacons;
    }

    public int countInteractions() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "select (count( distinct " + BeaconEntry.COLUMN_NAME_IDENTIFIER +
                        " )) as " + BeaconEntry.COUNT_BEACONS +
                        " from " + BeaconEntry.TABLE_NAME, null);
        cursor.moveToFirst();

        //TODO: to test
        int count = cursor.getInt(cursor.getColumnIndex(BeaconEntry.COUNT_BEACONS));
        cursor.close();
        return count;
    }

    public int countDailyInteractions(){
        return countInteractions(Helper.getToday());
    }

    public int countInteractions(Date fromTimestamp){
        SQLiteDatabase db = this.getReadableDatabase();
        int tmp = (int)fromTimestamp.getTime() / 1000;

        Cursor cursor = db.rawQuery(
                "select (count( distinct " + BeaconEntry.COLUMN_NAME_IDENTIFIER +
                        " )) as " + BeaconEntry.COUNT_BEACONS +
                        " from " + BeaconEntry.TABLE_NAME +
                        " where timestamp >= " + tmp, null);
        cursor.moveToFirst();

        //TODO: to test
        int count = cursor.getInt(cursor.getColumnIndex(BeaconEntry.COUNT_BEACONS));
        cursor.close();
        return count;
    }
}
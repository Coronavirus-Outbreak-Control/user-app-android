package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.util.Log;

import com.example.coronavirusherdimmunity.BuildConfig;
import com.example.coronavirusherdimmunity.PreferenceManager;
import com.example.coronavirusherdimmunity.enums.Distance;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class BeaconDto {

    public long timestmp;
    public long identifier;
    public int rssi;
    public Distance distance;
    public double distanceValue;
    public int interval;
    public double x = 0;
    public double y = 0;

    public BeaconDto(long identifier, int rssi, Distance distance, double distanceValue, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = new Date().getTime() / 1000;
        Log.i("BEACONTIME", ""+timestmp);
        this.distance = distance;
        this.distanceValue = distanceValue;
        this.x = x;
        this.y = y;
    }

    public BeaconDto(long identifier, int rssi, long timestamp, Distance distance, double distanceValue, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp;
        this.distance = distance;
        this.distanceValue = distanceValue;
        this.x = x;
        this.y = y;

    }

    public BeaconDto(long identifier, int rssi, Date timestamp, Distance distance, double distanceValue, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp.getTime() / 1000;
        this.distance = distance;
        this.distanceValue = distanceValue;
        this.x = x;
        this.y = y;
    }

    public JSONObject getJSON(Context context){
        /*
        let i: Int64  // id of this device
        let o: Int64  // id of the interacted device
        let w: Int64  //unix time expressed in seconds
        let y: Double? //latitude of the position at interaction time: avoid if not available
        let x: Double? //longitude of the position at interaction time: avoid if not available
        let t: Int    // time of interaction, default is 10
        let r: Int64 // rssi value
        "p": string // a for android or i for ios
         */
        try {
            JSONObject obj = new JSONObject();

            obj.put("o", this.identifier);
            obj.put("w", this.timestmp);
            obj.put("t", this.interval);
            obj.put("r", Math.abs(this.rssi));

            obj.put("s", new BigDecimal(this.distanceValue).setScale(1, RoundingMode.HALF_UP).doubleValue());
            obj.put("x", new BigDecimal(this.x).setScale(5, RoundingMode.HALF_UP).doubleValue());
            obj.put("y", new BigDecimal(this.y).setScale(5, RoundingMode.HALF_UP).doubleValue());

            return obj;
        }catch (Exception e){
            return null;
        }
    }

}

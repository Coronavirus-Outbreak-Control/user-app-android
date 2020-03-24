package com.example.coronavirusherdimmunity.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import com.example.coronavirusherdimmunity.PreferenceManager;
import com.example.coronavirusherdimmunity.enums.Distance;

import org.json.JSONObject;

import java.util.Date;

public class BeaconDto {

    public long timestmp;
    public int identifier;
    public int rssi;
    public Distance distance;
    public int interval;
    public double x = 0;
    public double y = 0;

    public BeaconDto(int identifier, int rssi, Distance distance, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = new Date().getTime() / 1000;
        this.distance = distance;
        this.x = x;
        this.y = y;
    }

    public BeaconDto(int identifier, int rssi, long timestamp, Distance distance, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp;
        this.distance = distance;
        this.x = x;
        this.y = y;

    }

    public BeaconDto(int identifier, int rssi, Date timestamp, Distance distance, double x, double y){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp.getTime() / 1000;
        this.distance = distance;
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

            obj.put("i", new PreferenceManager(context).getDeviceId());

            obj.put("o", this.identifier);
            obj.put("w", this.timestmp);
            obj.put("t", this.interval);
            obj.put("r", this.rssi);
            obj.put("p", "a");
            obj.put("d", distance.toString());
            obj.put("x", this.x);
            obj.put("y", this.y);
            return obj;
        }catch (Exception e){
            return null;
        }
    }

}

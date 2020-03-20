package com.example.coronavirusherdimmunity.utils;

import android.content.Context;

import com.example.coronavirusherdimmunity.PreferenceManager;
import com.example.coronavirusherdimmunity.enums.Distance;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.Date;

public class BeaconDto {

    public long timestmp;
    public int identifier;
    public int rssi;
    public Distance distance;
    public int interval;

    public BeaconDto(int identifier, int rssi, Distance distance){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = new Date().getTime() / 1000;
        this.distance = distance;
    }

    public BeaconDto(int identifier, int rssi, long timestamp, Distance distance){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp;
        this.distance = distance;
    }

    public BeaconDto(int identifier, int rssi, Date timestamp, Distance distance){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp.getTime() / 1000;
        this.distance = distance;
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

            return obj;
        }catch (Exception e){
            return null;
        }
    }

}

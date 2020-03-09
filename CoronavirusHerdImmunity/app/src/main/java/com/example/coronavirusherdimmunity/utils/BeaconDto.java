package com.example.coronavirusherdimmunity.utils;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.Date;

public class BeaconDto {

    public int timestmp;
    public int identifier;
    public int rssi;

    public BeaconDto(int identifier, int rssi){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = (int)new Date().getTime() / 1000;
    }

    public BeaconDto(int identifier, int rssi, int timestamp){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = timestamp;
    }

    public BeaconDto(int identifier, int rssi, Date timestamp){
        this.identifier = identifier;
        this.rssi = rssi;
        this.timestmp = (int)timestamp.getTime() / 1000;
    }

    public JSONObject getJSON(){
        /*
        let i: Int64  // id of this device
        let o: Int64  // id of the interacted device
        let w: Int64  //unix time expressed in seconds
        let y: Double? //latitude of the position at interaction time: avoid if not available
        let x: Double? //longitude of the position at interaction time: avoid if not available
        let t: Int    // time of interaction, default is 10
        let r: Int64 // rssi value
         */
        try {
            JSONObject obj = new JSONObject();

            // TODO : read my identifier
            obj.put("i", 0);

            obj.put("o", this.identifier);
            obj.put("w", this.timestmp);
            obj.put("t", 10);
            obj.put("r", this.rssi);

            return obj;
        }catch (Exception e){
            return null;
        }
    }

}

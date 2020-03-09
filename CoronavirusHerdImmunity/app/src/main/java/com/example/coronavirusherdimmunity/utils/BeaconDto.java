package com.example.coronavirusherdimmunity.utils;

import org.json.JSONObject;
import org.json.simple.JSONValue;

import java.util.Date;

public class BeaconDto {

    public Date timestmp;
    public int identifier;
    public int rssi;

    public BeaconDto(){}

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

            obj.put("o", identifier);
            obj.put("w", this.timestmp.getTime() / 1000);
            obj.put("t", 10);
            obj.put("r", rssi);

            return obj;
        }catch (Exception e){
            return null;
        }
    }

}

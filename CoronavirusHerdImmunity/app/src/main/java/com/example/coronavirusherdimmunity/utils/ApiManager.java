package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import org.json.*;
import org.json.simple.JSONValue;

// https://www.javatpoint.com/java-json-example
// https://square.github.io/okhttp/

public class ApiManager {

    private static final String baseEndoint = "https://api.coronaviruscheck.org";
    private static final MediaType JSONContentType = MediaType.parse("application/json; charset=utf-8");

    public static JSONObject registerDevice(String deviceId){

        Map device = new HashMap();
        device.put("manufacturer", Build.MANUFACTURER);
        device.put("model", Build.MODEL);

        Map os = new HashMap();
        os.put("name", "Android");
        os.put("version", Build.VERSION.RELEASE);

        Map body = new HashMap();
        body.put("id", deviceId);
        body.put("os", os);
        body.put("device", device);

        OkHttpClient client = new OkHttpClient();

        RequestBody rq = RequestBody.create(JSONContentType, JSONValue.toJSONString(body));
        Request request = new Request.Builder()
                .url(baseEndoint + "/device/handshake")
                .post(rq)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String strResponse = response.body().string();
            JSONObject obj = new JSONObject(strResponse);
            return obj;
        }catch(Exception e){
            Log.d("CHI", "EXCEPTION on registering device");
        }
        return null;
    }

    public static JSONObject pushInteractions(Context context, List<BeaconDto> beacons, String authToken){
        // TODO: to test request
        OkHttpClient client = new OkHttpClient();

        if(beacons == null || beacons.size() == 0){
            return null;
        }

        JSONArray arr = new JSONArray();
        for (BeaconDto beacon: beacons) {
            if(beacon.getJSON(context) != null){
                arr.put(beacon.getJSON(context));
            }
        }

        RequestBody rq = RequestBody.create(JSONContentType, JSONValue.toJSONString(arr));
        Request request = new Request.Builder()
                .url(baseEndoint + "/interaction/report")
                .addHeader("Authorization", "Bearer " + authToken)
                .post(rq)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String strResponse = response.body().string();
            JSONObject obj = new JSONObject(strResponse);
            return obj;
        }catch(Exception e){
            Log.d("CHI", "EXCEPTION on pushing interaction");
        }
        return null;
    }

}

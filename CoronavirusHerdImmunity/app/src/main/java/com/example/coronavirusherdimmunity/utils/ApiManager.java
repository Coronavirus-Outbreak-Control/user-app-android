package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.coronavirusherdimmunity.BuildConfig;
import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.PreferenceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
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

    private static CovidApplication instance;

    public static CovidApplication getInstance() {
        return instance;
    }
    public static Context getContext(){
        return instance;
    }

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
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpInterceptor()).build();

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
            if (response.code() == 200) {
                String strResponse = response.body().string();
                JSONObject obj = new JSONObject(strResponse);
                return obj;
            } else
                return null;
        }catch(Exception e){
            Log.d("CHI", "EXCEPTION on pushing interaction");
        }
        return null;
    }

    public static JSONObject registerPushToken(int deviceId, String token, String authToken) {
        Map body = new HashMap();
        body.put("id", deviceId);
        body.put("push_id", token);
        body.put("platform", "android");

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpInterceptor()).build();

        RequestBody rq = RequestBody.create(JSONContentType, JSONValue.toJSONString(body));
        Request request = new Request.Builder()
                .url(baseEndoint + "/device")
                .addHeader("Authorization", "Bearer " + authToken)
                .put(rq)
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

    private static class HttpInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            //Build new request
            Request.Builder builder = request.newBuilder();
            builder.header("Accept", "application/json"); //if necessary, say to consume JSON

            String token = new PreferenceManager(CovidApplication.getContext()).getAuthToken(); //save token of this request for future
            setAuthHeader(builder, token); //write current token to request

            request = builder.build(); //overwrite old request
            Response response = chain.proceed(request); //perform request, here original request will be executed

            if (response.code() == 401) { //if unauthorized
                synchronized (this) { //perform all 401 in sync blocks, to avoid multiply token updates
                    String code = null;
                    try {
                        code = refreshToken();
                        if (code == null){
                            return response;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            return response;
        }

        private void setAuthHeader(Request.Builder builder, String token) {
            if (token != null) //Add Auth token to each request if authorized
                builder.header("Authorization", String.format("Bearer %s", token));
        }

        private String refreshToken() throws JSONException {
            //Refresh token, synchronously, save it, and return result code
            //you might use retrofit here
            String deviceUUID = new PreferenceManager(instance.getApplicationContext()).getDeviceUUID();
            JSONObject object = registerDevice(BuildConfig.DEBUG ? "06c9cf6c-ecfb-4807-afb4-4220d0614593" : deviceUUID);
            if (object != null) {
                if (object.has("token")){
                    String token = object.getString("token");
                    new PreferenceManager(CovidApplication.getContext()).setAuthToken(token);
                    return token;
                }
            }
            return null;
        }

//        private int logout() {
//            //logout your user
//        }
    }
}

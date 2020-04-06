package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.coronavirusherdimmunity.BuildConfig;
import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.PreferenceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;

import org.json.*;
import org.json.simple.JSONValue;

// https://www.javatpoint.com/java-json-example
// https://square.github.io/okhttp/


public class ApiManager {

    private static final String baseEndoint = "https://api.coronaviruscheck.org";
    private static final MediaType JSONContentType = MediaType.parse("application/json; charset=utf-8");

    public static JSONObject registerDevice(String deviceId, String challenge){

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
        body.put("challenge", challenge);

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

        JSONObject body = new JSONObject();

        try {
            body.put("i", new PreferenceManager(context).getDeviceId());
            body.put("p", "a");
            body.put("v", BuildConfig.VERSION_CODE);
            body.put("z", arr);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody rq = RequestBody.create(JSONContentType, JSONValue.toJSONString(body));
        try {
            Log.i("BEACONTIME PUSH boby", requestBodyToString(rq));
        } catch (IOException e) {
            e.printStackTrace();
        }


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

    public static String requestBodyToString(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return buffer.readUtf8();
    }

    public static JSONObject registerPushToken(Long deviceId, String token, String authToken) {
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
                Log.i("PUSH","refresh token");
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
            String deviceUUID = new PreferenceManager(CovidApplication.getContext()).getDeviceUUID();
            String challenge = new PreferenceManager(CovidApplication.getContext()).getChallenge();
            JSONObject object = registerDevice(/*"06c9cf6c-ecfb-4807-afb4-4220d0614593"*/ deviceUUID, challenge);
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

    public static JSONObject downloadAlert(@NonNull String url, int filterId, String remoteLanguage){
        String myLocalizedUrl = url.replace("{language}", Locale.getDefault().getLanguage());
        String remoteLocalizedUrl = url.replace("{language}", remoteLanguage);


        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpInterceptor()).build();
        Request request = new Request.Builder()
                .url(myLocalizedUrl)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            String strResponse;
            if (response.code() == 200){
                strResponse = response.body().string();
            } else {
                request = new Request.Builder()
                        .url(remoteLocalizedUrl)
                        .addHeader("Content-Type", "application/json")
                        .get()
                        .build();
                response = client.newCall(request).execute();
                strResponse = response.body().string();
            }

            JSONObject obj = new JSONObject(strResponse);
            JSONArray filters = obj.getJSONArray("filters");
            JSONObject res = null;
            for (int i=0; i<filters.length(); i++) {
                JSONObject o = filters.getJSONObject(i);
                if (o.has("filter_id") && o.getInt("filter_id")==filterId){
                    String filterLang = o.getString("language");
                    JSONObject content = o.getJSONObject("content");

                    res = content.has(Locale.getDefault().getLanguage()) ? content.getJSONObject(Locale.getDefault().getLanguage()) : content.getJSONObject(filterLang);
                }
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            Log.d("CHI", "EXCEPTION downloading url");
        }
        return null;
    }
}

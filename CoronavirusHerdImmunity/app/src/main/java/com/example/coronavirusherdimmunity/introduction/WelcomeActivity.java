package com.example.coronavirusherdimmunity.introduction;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.coronavirusherdimmunity.CovidApplication;
import com.example.coronavirusherdimmunity.HowItWorksActivity;
import com.example.coronavirusherdimmunity.MonitoringActivity;
import com.example.coronavirusherdimmunity.PreferenceManager;
import com.example.coronavirusherdimmunity.R;
import com.example.coronavirusherdimmunity.utils.ApiManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;


public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.intro0_welcome);


        this.writeTitle();


        Button start_button = (Button) findViewById(R.id.button_next);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick_registerDevice();    //if reCaptcha has been success -> register the device and go to next activity
            }
        });

        Button how_it_works_button = (Button) findViewById(R.id.how_it_works);
        how_it_works_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, HowItWorksActivity.class));
            }
        });

    }


    private void writeTitle() {

        String first = getResources().getString(R.string.welcome_first);
        String pink = getResources().getString(R.string.welcome_next);
        String last = getResources().getString(R.string.welcome_last);
        TextView t = (TextView) findViewById(R.id.welcome_to);

        t.setText(Html.fromHtml(first +
                "<br/><font color='#FF6F61'> " + pink + "</font><br/>"
                + last));
    }


    /**
     * Manage click on reCaptcha and register device
     * If reCaptcha has been success then the device is registered and go to next activity
     */
    public void onClick_registerDevice() {
        String API_SITE_KEY = "6LcxTeUUAAAAAFeCan-0kQdEhz0_B6wtlPFCFfq3";

        //check reCaptcha
        SafetyNet.getClient(this).verifyWithRecaptcha(API_SITE_KEY)
                .addOnSuccessListener( this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {

                                // Indicates communication with reCAPTCHA service was successful.
                                final String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Received reCaptcha token and This token still needs to be validated on the server using the SECRET key api.
                                    Log.i("ReCaptcha", "onSuccess: " + response.getTokenResult());

                                    Task.callInBackground(new Callable<Long>() {
                                        @Override
                                        public Long call() throws Exception {

                                            new PreferenceManager(CovidApplication.getContext()).setChallenge(userResponseToken); //save 'challenge' on shared preference

                                            String deviceUUID = new PreferenceManager(getApplicationContext()).getDeviceUUID();
                                            String challenge = userResponseToken;
                                            if (challenge != null) {
                                                JSONObject object = ApiManager.registerDevice(/*"06c9cf6c-ecfb-4807-afb4-4220d0614593"*/ deviceUUID, challenge); //call registerDevice
                                                if (object != null) {
                                                    if (object.has("token")) {
                                                        try {
                                                            new PreferenceManager(getApplicationContext()).setAuthToken(object.getString("token"));  //save auth token in shared preferences

                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    return object.getLong("id");
                                                }
                                            }
                                            return Long.valueOf(-1);

                                        }
                                    }).onSuccess(new Continuation<Long, Object>() {
                                        @Override
                                        public Object then(Task<Long> task) {
                                            Log.e("CovidApp", "dev " + task.getResult());

                                            if (task.getResult() != -1) {
                                                new PreferenceManager(getApplicationContext()).setDeviceId(task.getResult());   //save device id in shared preferences
                                                CovidApplication application = ((CovidApplication) getApplicationContext());
                                                application.initBeacon(task.getResult());

                                                startActivity(new Intent(getApplicationContext(), BluetoothActivity.class));    //go to bluetooth activity
                                            }
                                            return null;
                                        }
                                    });

                                }
                            }
                        })
                .addOnFailureListener( this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d("ReCaptcha", "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d("ReCaptcha", "Error: " + e.getMessage());
                        }
                    }
                });

    }
}
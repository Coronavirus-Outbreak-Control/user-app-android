package com.example.coronavirusherdimmunity.introduction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.altbeacon.beacon.BeaconManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import bolts.Continuation;
import bolts.Task;


public class WelcomeActivity extends AppCompatActivity {

    private RelativeLayout progBar;

    private Button start_button;
    private Button how_it_works_button;

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

        progBar = (RelativeLayout) findViewById(R.id.rel_progbar);
        progBar.setVisibility(View.GONE);  //set invisible the relative layout (progress bar + text view)

        start_button = (Button) findViewById(R.id.button_next);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick_registerDevice();    //if reCaptcha has been success -> register the device and go to next activity
            }
        });

        how_it_works_button = (Button) findViewById(R.id.how_it_works);
        how_it_works_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, HowItWorksActivity.class));
            }
        });

    }


    private void writeTitle() {
        TextView t = (TextView) findViewById(R.id.welcome_to);
        Spanned text = Html.fromHtml(getResources().getString(R.string.welcome));
        t.setText(text);
    }


    /**
     * Manage click on reCaptcha and register device
     * If reCaptcha has been success then the device is registered and go to next activity
     */
    public void onClick_registerDevice() {
        String API_SITE_KEY = "6LcxTeUUAAAAAFeCan-0kQdEhz0_B6wtlPFCFfq3";

        //if 'device id' is unknown then check reCaptcha and call register device in order to get device id and auth token
        if (new PreferenceManager(getApplicationContext()).getDeviceId() == -1) {

            //check reCaptcha
            SafetyNet.getClient(this).verifyWithRecaptcha(API_SITE_KEY)
                    .addOnSuccessListener(this,
                            new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                                @Override
                                public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {

                                    // Indicates communication with reCAPTCHA service was successful.
                                    final String userResponseToken = response.getTokenResult();
                                    if (!userResponseToken.isEmpty()) {
                                        // Received reCaptcha token and This token still needs to be validated on the server using the SECRET key api.
                                        Log.i("ReCaptcha", "onSuccess: " + response.getTokenResult());

                                        new PreferenceManager(CovidApplication.getContext()).setChallenge(userResponseToken); //save 'challenge' on shared preference

                                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);  //disable interaction with UI when progress bar is loading
                                        progBar.setVisibility(View.VISIBLE);  //set visible the relative layout (progress bar + text view)

                                        Task.callInBackground(new Callable<Long>() {
                                            @Override
                                            public Long call() throws Exception {

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
                                                return -1L;

                                            }
                                        }).onSuccess(new Continuation<Long, Object>() {
                                            @Override
                                            public Object then(Task<Long> task) {
                                                Log.e("CovidApp", "dev " + task.getResult());

                                                long res = task.getResult();
                                                if (res != -1) {  //case on success (have got "token + device id")
                                                    new PreferenceManager(getApplicationContext()).setDeviceId(task.getResult());   //save device id in shared preferences

                                                    //if init beacon has not been already started
                                                    if (!BeaconManager.getInstanceForApplication(getApplicationContext()).isAnyConsumerBound()) {
                                                        CovidApplication application = ((CovidApplication) getApplicationContext());
                                                        application.initBeacon(res);
                                                    }

                                                    startActivity(new Intent(WelcomeActivity.this, BluetoothActivity.class));    //go to bluetooth activity

                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //reenable interaction with UI
                                                    progBar.setVisibility(View.GONE);  //set invisible the relative layout (progress bar + text view)
                                                }
                                                else{           //case on failure

                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //reenable interaction with UI
                                                    progBar.setVisibility(View.GONE);  //set invisible the relative layout (progress bar + text view)

                                                    Toast.makeText(getApplicationContext(), R.string.toast_error_regdev,Toast.LENGTH_SHORT).show();
                                                }
                                                return null;

                                            }
                                        },Task.UI_THREAD_EXECUTOR);

                                    }
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
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
        else{  //else if 'device id' has been already known then go to next activity

            startActivity(new Intent(WelcomeActivity.this, BluetoothActivity.class));    //go to bluetooth activity
        }
    }
}
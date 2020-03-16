package com.example.coronavirusherdimmunity.introduction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.coronavirusherdimmunity.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LocationActivity  extends AppCompatActivity {

    private final int REQUEST_ID_PERMISSION_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.intro2_location);

        Button button_next, button_skip;


        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestLocationPermission();

            }
        });

        button_skip = findViewById(R.id.button_skip);
        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LocationActivity.this, NotificationsActivity.class));
                finish();
            }
        });

    }


    /**
     * Require Location permission, turn on it, go to next activity
     */
    private void requestLocationPermission(){

        //if location permission is not granted then request permission
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(LocationActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ID_PERMISSION_LOCATION);

        }else{ //already granted, thus go to next activity
            startActivity(new Intent(LocationActivity.this, NotificationsActivity.class));
            finish();
        }
    }


    /**
     * When the user responds to your app's permission request, the system invokes this function.
     * This function check if the permissions are granted or not.
     * If they are granted then turn location and go to next activity
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_PERMISSION_LOCATION: {
                // if permission was granted then turn on location
                if (grantResults.length > 0 &&
                        grantResults[0] != PackageManager.PERMISSION_GRANTED){

                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //turn on location

                } else {    // permission denied
                    startActivity(new Intent(LocationActivity.this, NotificationsActivity.class));
                    finish();
                }
                return;
            }
        }
    }

}

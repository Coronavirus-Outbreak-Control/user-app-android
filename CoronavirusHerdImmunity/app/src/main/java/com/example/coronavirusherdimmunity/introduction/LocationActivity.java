package com.example.coronavirusherdimmunity.introduction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.coronavirusherdimmunity.MainActivity;
import com.example.coronavirusherdimmunity.R;

import java.util.ArrayList;

public class LocationActivity  extends AppCompatActivity {

    private final int REQUEST_ID_PERMISSION_LOCATION = 2;

    private int lenght_listPermissionsNeeded = 0;
    private Bundle bundle;

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

        bundle = getIntent().getExtras(); //Retrieves data from the intent

        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestLocationPermission();  //require Local Permissions

            }
        });

        button_skip = findViewById(R.id.button_skip);
        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                go_nextActivity();
            }
        });

    }

    /**
     * if this activity has been re-called in order to enable permission then go to MainActivity
     * else if this activity has been called for the first time then go NotificationsActivity
     */
    private void go_nextActivity(){

        if ( bundle != null &&
                bundle.getBoolean("permission_request")){ // if this activity has been recalled then go to MainActivity

            startActivity(new Intent(LocationActivity.this, MainActivity.class));
            finish();

        }else{ //if this activity has been called for the first time then go to NotificationsActivity

            startActivity(new Intent(LocationActivity.this, NotificationsActivity.class));
            finish();
        }

    }

    /**
     * Require Location permission, turn on it, go to next activity
     */
    private void requestLocationPermission(){

        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        //if location permission is not granted then request permission
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }

        lenght_listPermissionsNeeded = listPermissionsNeeded.size();
        //if the permission list is not empty then requires the permissions
        if (!listPermissionsNeeded.isEmpty()) {

            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_PERMISSION_LOCATION);

        } else{ //already granted, thus go to next activity
            go_nextActivity();
        }

    }


    /*+
     * If every permission is granted  by user then return true,
     * else return false
     */
    private boolean check_permission_granted(int[] grantResults){
        for (int i = 0; i < lenght_listPermissionsNeeded; i++ ) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
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
                // if permissions were granted then go next Activity
                if (grantResults.length > 0 &&
                        check_permission_granted(grantResults)) {

                    go_nextActivity();

                }
            }
        }
    }

}

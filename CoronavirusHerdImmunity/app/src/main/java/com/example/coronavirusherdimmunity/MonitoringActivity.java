package com.example.coronavirusherdimmunity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coronavirusherdimmunity.utils.PermissionRequest;

import org.altbeacon.beacon.BeaconManager;

public class MonitoringActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }


    protected static final String TAG = "MonitoringActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
    }


    public void onRangingClicked(View view) {
        Intent myIntent = new Intent(this, RangingActivity.class);
        this.startActivity(myIntent);
    }

    public void onEnableClicked(View view) {
        CovidApplication application = ((CovidApplication) this.getApplicationContext());
        if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
            application.disableMonitoring();
            ((Button)findViewById(R.id.enableButton)).setText(R.string.bt_enable_mon);
        }
        else {
            ((Button)findViewById(R.id.enableButton)).setText(R.string.bt_disable_mon);
            application.enableMonitoring();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        CovidApplication application = ((CovidApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
        updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((CovidApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

    public void updateLog(final String log) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = findViewById(R.id.monitoringText);
                editText.setText(log);
            }
        });
    }

}

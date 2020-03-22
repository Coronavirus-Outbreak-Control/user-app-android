package com.example.coronavirusherdimmunity.introduction;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.coronavirusherdimmunity.HowItWorksActivity;
import com.example.coronavirusherdimmunity.MainActivity;
import com.example.coronavirusherdimmunity.MonitoringActivity;
import com.example.coronavirusherdimmunity.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

    private final int REQUEST_ID_PERMISSIONS_BLUETOOTH = 1;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.intro1_bluetooth);

        Button button_next, button_skip;

        bundle = getIntent().getExtras(); //Retrieves data from the intent

        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBTPermission(); //requires bluetooth permissions and turn on
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
     * else if this activity has been called for the first time then go LocationActivity
     */
    private void go_nextActivity(){
        if (bundle != null &&
                bundle.getBoolean("permission_request")) { // if this activity has been recalled then go to MainActivity
            Intent intent = new Intent(BluetoothActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else { //if this activity has been called for the first time then go to LocationActivity
            startActivity(new Intent(BluetoothActivity.this, LocationActivity.class));
        }
    }


    /**
     * Require Bluetooth permission, turn on it, go to next activity
     */
    private void requestBTPermission(){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) { // Device doesn't support Bluetooth

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.blue_notsupported);
            builder.setMessage(R.string.blue_msg_notsupported);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
            builder.show();
        } else {
            if (!bluetoothAdapter.isEnabled()) { // if bluetooth is not enabled then turn on

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ID_PERMISSIONS_BLUETOOTH);

            } else { //else bluetooth is enabled then go to next activity

                go_nextActivity();

            }
        }
    }


    /**
     * When the user responds to your app's permission request, the system invokes this function.
     * This function check if the permissions are granted, then go to next activity
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ID_PERMISSIONS_BLUETOOTH: {
                // if permission was granted then go to next Activity
                if (resultCode == RESULT_OK) {

                    go_nextActivity();

                }
            }
        }
    }

}


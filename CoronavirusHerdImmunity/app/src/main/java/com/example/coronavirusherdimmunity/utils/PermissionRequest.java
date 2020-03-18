package com.example.coronavirusherdimmunity.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import com.example.coronavirusherdimmunity.R;
import com.example.coronavirusherdimmunity.introduction.BluetoothActivity;
import com.example.coronavirusherdimmunity.introduction.LocationActivity;


public class PermissionRequest {

    private Context context;

    public PermissionRequest(Context packageContext){
        this.context = packageContext;
    }

    /**
     * Check permissions if they are granted else go to introduction activities in order to enable them (Bluetooth, Location)
     */
    public void checkPermissions(){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) { // if bluetooth is not enabled go to bluetooth activity in order to enable it

            // show alert dialog "Please, enable bluetooth"
            final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle(R.string.blue_disabled);
            builder.setMessage(R.string.blue_please_en);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //when you click "ok" then go to next activity in order to enable bluetooth
                    context.startActivity(new Intent(context, BluetoothActivity.class));
                }
            });
            builder.show();

        } //else if location is not enabled go to location activity in order to enable it
        else if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){

            // show alert dialog "Please, enable location"
            final AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle(R.string.loc_disabled);
            builder.setMessage(R.string.loc_please_en);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //when you click "ok" then go to Location Activity in order to enable location permission
                    context.startActivity(new Intent(context, LocationActivity.class));
                }
            });
            builder.show();

        }else{

        }
    }


}

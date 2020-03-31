package com.example.coronavirusherdimmunity.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;

import androidx.core.app.ActivityCompat;

import com.example.coronavirusherdimmunity.R;
import com.example.coronavirusherdimmunity.introduction.BluetoothActivity;
import com.example.coronavirusherdimmunity.introduction.LocationActivity;


public class PermissionRequest {

    private Context context;

    public PermissionRequest(Context packageContext) {
        this.context = packageContext;
    }

    /**
     * Check permissions if they are granted else go to introduction activities in order to enable them (Bluetooth, Location)
     * @param: active_permission if 'true' the function checks permissions and enable them, else if 'false' it checks just permission
     * @return: 'true' if all permissions are granted, 'false' if at least one permission is not granted
     */
    public boolean checkPermissions(Boolean active_permission) {

        boolean ret_check_perm = true;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        } else if (!bluetoothAdapter.isEnabled()) { // if bluetooth is not enabled go to bluetooth activity in order to enable it

            if (active_permission) { // if 'active_permission' is true then requires to enable permission

                final Intent intent_bt = new Intent(context, BluetoothActivity.class);
                intent_bt.putExtra("permission_request", true); // notify next activity that permission is required

                // show alert dialog "Please, enable bluetooth"
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.blue_disabled);
                builder.setMessage(R.string.blue_please_en);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //when you click "ok" then go to next activity in order to enable bluetooth
                        context.startActivity(intent_bt);
                        ((Activity) context).finish();
                    }
                });
                builder.setCancelable(false);
                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                        if(keyCode == KeyEvent.KEYCODE_BACK){ // if you click back button then close dialog
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
                builder.show();
            }

            ret_check_perm = false;

        }//if location is not enabled go to location activity in order to enable it
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (active_permission) { // if 'active_permission' is true then requires to enable permission

                    final Intent intent_loc = new Intent(context, LocationActivity.class);
                    intent_loc.putExtra("permission_request", true); // notify next activity that permission is required

                    // show alert dialog "Please, enable location"
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.loc_disabled);
                    builder.setMessage(R.string.loc_please_en);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //when you click "ok" then go to Location Activity in order to enable location permission
                            context.startActivity(intent_loc);
                            ((Activity) context).finish();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                            if(keyCode == KeyEvent.KEYCODE_BACK){ // if you click back button then close dialog
                                dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    builder.show();
                }

                ret_check_perm = false;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (active_permission) { // if 'active_permission' is true then requires to enable permission

                    final Intent intent_loc = new Intent(context, LocationActivity.class);
                    intent_loc.putExtra("permission_request", true); // notify next activity that permission is required

                    // show alert dialog "Please, enable location"
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.loc_disabled);
                    builder.setMessage(R.string.loc_please_en);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //when you click "ok" then go to Location Activity in order to enable location permission
                            context.startActivity(intent_loc);
                            ((Activity) context).finish();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                            if(keyCode == KeyEvent.KEYCODE_BACK){ // if you click back button then close dialog
                                dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    builder.show();
                }

                ret_check_perm = false;
            }
        }

        return ret_check_perm;
    }


}

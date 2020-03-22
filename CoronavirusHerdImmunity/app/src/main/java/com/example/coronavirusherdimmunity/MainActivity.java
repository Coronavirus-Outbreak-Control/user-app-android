package com.example.coronavirusherdimmunity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.coronavirusherdimmunity.utils.ApiManager;
import com.example.coronavirusherdimmunity.utils.PermissionRequest;
import com.example.coronavirusherdimmunity.utils.QRCodeGenerator;
import com.example.coronavirusherdimmunity.utils.StorageManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.Callable;

import bolts.Continuation;


public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        this.writeQRCode();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("COVAPP", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        final String token = task.getResult().getToken();

                        bolts.Task.callInBackground(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                int deviceId = new PreferenceManager(mContext).getDeviceId();
                                ApiManager.registerPushToken(deviceId, token, new PreferenceManager(mContext).getAuthToken());
                                return null;
                            }
                        }).onSuccess(new Continuation<Object, Object>() {
                            @Override
                            public Object then(bolts.Task<Object> task) throws Exception {
                                return null;
                            }
                        });
                    }
                });


        if (BuildConfig.DEBUG){
            findViewById(R.id.openMonitoring).setVisibility(View.VISIBLE);
            findViewById(R.id.openMonitoring).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(mContext, MonitoringActivity.class);
                    startActivity(myIntent);
                }
            });
        } else {
            findViewById(R.id.openMonitoring).setVisibility(View.GONE);
        }

        Button how_it_works_button = (Button) findViewById(R.id.how_it_works);
        how_it_works_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HowItWorksActivity.class));
            }
        });


        PermissionRequest permissions = new PermissionRequest(MainActivity.this);
        permissions.checkPermissions(); //check if bluetooth and location are enabled else go to activity in order to enable them
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            writeInteractions();
            writePatientStatus();
        }
    };

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("STATUS_UPDATE"));
        super.onResume();
        writeInteractions();
        writePatientStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void writePatientStatus() {
        TextView statusTextView = (TextView) findViewById(R.id.status_value);

        String status = new PreferenceManager(getApplicationContext()).getPatientStatus().toString();
        statusTextView.setText(String.valueOf(status));
        //PatientStatus.setTextColor();
    }

    private void writeInteractions() {
        TextView interactionsTextView = (TextView) findViewById(R.id.n_interactions);

        int interactions = new StorageManager(getApplicationContext()).countInteractions();
        interactionsTextView.setText(String.valueOf(interactions));
    }

    private void writeQRCode() {
        ImageView qrImage = (ImageView) findViewById(R.id.qr_code);

        int deviceId = new PreferenceManager(mContext.getApplicationContext()).getDeviceId();

        QRCodeGenerator generator = new QRCodeGenerator(mContext);
        generator.generateQRCode(deviceId, qrImage);
    }

}

package com.example.coronavirusherdimmunity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.coronavirusherdimmunity.enums.PatientStatus;
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
import static com.example.coronavirusherdimmunity.R.*;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Context mContext;
    final String MESSAGE = "Help us. Together we can save lives. https://coronavirus-outbreak-control.github.io/web/index.html";
    final String LABEL = "Coronavirus Outbreak Control Link";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

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
            findViewById(id.openMonitoring).setVisibility(View.VISIBLE);
            findViewById(id.openMonitoring).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(mContext, MonitoringActivity.class);
                    startActivity(myIntent);
                }
            });
        } else {
            findViewById(id.openMonitoring).setVisibility(View.GONE);
        }


        PermissionRequest permissions = new PermissionRequest(MainActivity.this);
        permissions.checkPermissions(true); //check if bluetooth and location are enabled else go to activity in order to enable them


        /* BUTTONS */

        findViewById(id.how_it_works).setOnClickListener(this);
        findViewById(id.facebook).setOnClickListener(this);
        findViewById(id.twitter).setOnClickListener(this);
        findViewById(id.linkedin).setOnClickListener(this);
        findViewById(id.messenger).setOnClickListener(this);
        findViewById(id.whatsapp).setOnClickListener(this);
        findViewById(id.sms).setOnClickListener(this);
        findViewById(id.mail).setOnClickListener(this);
        findViewById(id.mail).setOnClickListener(this);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //writeInteractions();
            writeAppStatus();
            writePatientStatus();
        }
    };

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("STATUS_UPDATE"));
        super.onResume();
        //writeInteractions();
        writeAppStatus();
        writePatientStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void writePatientStatus() {

        TextView statusTextView = (TextView) findViewById(id.status_user);

        PatientStatus status = new PreferenceManager(getApplicationContext()).getPatientStatus();

        String color = this.getPatientStatusColor(status.toInt());

        statusTextView.setText(String.valueOf(status.toString()));
        statusTextView.setTextColor(Color.parseColor(color));
    }


    @SuppressLint("ResourceType")
    public String getPatientStatusColor(int status) {
        //{0: normal, 1: infected, 2: quarantine, 3: healed, 4: suspect}

        String black = getResources().getString(color.colorTextDark);
        String green = getResources().getString(R.color.green);
        String red = getResources().getString(R.color.red);
        String orange = getResources().getString(R.color.orange);
        String yellow = getResources().getString(R.color.yellow);

        switch (status) {
            case 0:
                return black;
            case 1:
                return red;
            case 2:
                return orange;
            case 3:
                return green;
            case 4:
                return yellow;
        }
        return black;
    }



    private void writeAppStatus() {
        TextView statusTextView = (TextView) findViewById(id.status_app);
        PermissionRequest permissions = new PermissionRequest(MainActivity.this);

        if (permissions.checkPermissions(true)) {
            statusTextView.setText(String.valueOf("Active"));

            int green = getResources().getColor(color.green);
            statusTextView.setTextColor(green);
        }
        else {
            statusTextView.setText(String.valueOf("Inactive"));
            int red = getResources().getColor(color.red);
            statusTextView.setTextColor(red);
        }
    }

    /*private void writeInteractions() {
        TextView interactionsTextView = (TextView) findViewById(R.id.n_interactions);

        int interactions = new StorageManager(getApplicationContext()).countInteractions();
        interactionsTextView.setText(String.valueOf(interactions));
    }*/

    private void writeQRCode() {
        ImageView qrImage = (ImageView) findViewById(id.qr_code);

        int deviceId = new PreferenceManager(mContext.getApplicationContext()).getDeviceId();

        QRCodeGenerator generator = new QRCodeGenerator(mContext);
        generator.generateQRCode(deviceId, qrImage);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case id.how_it_works:
                startActivity(new Intent(MainActivity.this, HowItWorksActivity.class));
                break;

            case id.facebook:
                // do your code
                break;

            case id.twitter:
                // do your code
                break;

            case id.linkedin:
                // do your code
                break;

            case id.messenger:
                // do your code
                break;

            case id.whatsapp:
                // do your code
                break;

            case id.sms:
                // do your code
                break;

            case id.mail:
                // do your code
                break;

            case id.link:
                //this.CopyToClipboard(this.LABEL, this.MESSAGE);
                break;
            default:
                break;
        }
    }

/*

    private void CopyToClipboard(String label, String text) {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }
*/

/*
    private void shareWith() {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }
*/

}

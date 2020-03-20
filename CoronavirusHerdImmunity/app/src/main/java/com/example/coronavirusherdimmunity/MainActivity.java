package com.example.coronavirusherdimmunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coronavirusherdimmunity.introduction.BluetoothActivity;
import com.example.coronavirusherdimmunity.utils.PermissionRequest;
import com.example.coronavirusherdimmunity.utils.QRCodeGenerator;
import com.example.coronavirusherdimmunity.utils.StorageManager;


public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        this.writeQRCode();
        this.writePatientStatus();
        this.writeInteractions();

        findViewById(R.id.openMonitoring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(mContext, MonitoringActivity.class);
                startActivity(myIntent);
            }
        });

        PermissionRequest permissions = new PermissionRequest(MainActivity.this);
        permissions.checkPermissions(); //check if bluetooth and location are enabled else go to activity in order to enable them
    }

    private void writePatientStatus() {
        TextView statusTextView = (TextView) findViewById(R.id.status_value);

        String status = new PreferenceManager(getApplicationContext()).getPatientStatus().toString();
        statusTextView.setText(String.valueOf(status));
        //PatientStatus.setTextColor();
    }

    private void writeInteractions() {
        TextView interactionsTextView = (TextView) findViewById(R.id.n_interactions);

        // TODO: use countTotalInteractions instead
        int interactions = new StorageManager(getApplicationContext()).countTotalInteractions();
        interactionsTextView.setText(String.valueOf(interactions));
    }

    private void writeQRCode() {
        ImageView qrImage = (ImageView) findViewById(R.id.qr_code);
        int deviceId = new PreferenceManager(mContext.getApplicationContext()).getDeviceId();

        QRCodeGenerator generator = new QRCodeGenerator(mContext);
        generator.generateQRCode(deviceId, qrImage);
    }

}

package com.example.coronavirusherdimmunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    }

    private void writePatientStatus() {

        String status = new PreferenceManager(getApplicationContext()).getPatientStatus().toString();
        TextView statusTextView = (TextView) findViewById(R.id.status_value);
        statusTextView.setText(status);
        //PatientStatus.setTextColor();
    }

    private void writeInteractions() {

        int interactions = new StorageManager(getApplicationContext()).countDailyInteractions();
        TextView interactionsTextView = (TextView) findViewById(R.id.n_interactions);
        interactionsTextView.setText(interactions);
    }

    private void writeQRCode() {

        QRCodeGenerator generator = new QRCodeGenerator(mContext);
        int deviceId = new PreferenceManager(mContext.getApplicationContext()).getDeviceId();
        ImageView qrImage = (ImageView) findViewById(R.id.qr_code);

        generator.generateQRCode(deviceId, qrImage);
    }

}

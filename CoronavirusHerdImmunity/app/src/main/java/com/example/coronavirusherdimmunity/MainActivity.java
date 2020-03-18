package com.example.coronavirusherdimmunity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.coronavirusherdimmunity.utils.QRCodeGenerator;


public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // QR Code stuff
        QRCodeGenerator gen = new QRCodeGenerator(mContext);
        int deviceId = new PreferenceManager(mContext.getApplicationContext()).getDeviceId();
        ImageView qrImage = (ImageView) findViewById(R.id.qr_code);

        gen.generateQRCode(deviceId, qrImage);


        findViewById(R.id.openMonitoring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(mContext, MonitoringActivity.class);
                startActivity(myIntent);
            }
        });

    }

}

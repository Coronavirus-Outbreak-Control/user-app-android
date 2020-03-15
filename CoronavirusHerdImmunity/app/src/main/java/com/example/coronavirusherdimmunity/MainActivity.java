package com.example.coronavirusherdimmunity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        findViewById(R.id.openMonitoring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(mContext, MonitoringActivity.class);
                startActivity(myIntent);
            }
        });
    }
}

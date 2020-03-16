package com.example.coronavirusherdimmunity.introduction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.coronavirusherdimmunity.MainActivity;
import com.example.coronavirusherdimmunity.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationsActivity extends AppCompatActivity {

    private final int REQUEST_ID_PERMISSION_NOTIFICATION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.intro3_notifications);

        Button button_next, button_skip;


        button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestNotificationPermission();
                //startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
                //finish();
            }
        });

        button_skip = findViewById(R.id.button_skip);
        button_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
                finish();
            }
        });

    }
    /**
     * Require and enable Notification permission, go to next activity
     */
    private void requestNotificationPermission() {

        //if location permission is not granted then request permission
        if (ActivityCompat.checkSelfPermission(NotificationsActivity.this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(NotificationsActivity.this,
                    new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                    REQUEST_ID_PERMISSION_NOTIFICATION);
        }

        NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext()).contains(getApplicationContext().getPackageName());

        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        if (!NotificationManagerCompat.getEnabledListenerPackages (getApplicationContext()).contains(getApplicationContext().getPackageName())) {
            //service is not enabled try to enabled by calling
            getApplicationContext().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            //Your own logic
        }
    }


    /**
     * When the user responds to your app's permission request, the system invokes this function.
     * This function check if the permissions are granted or not, then go to next activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_PERMISSION_NOTIFICATION: {
                startActivity(new Intent(NotificationsActivity.this, MainActivity.class));
                finish();
            }
        }
    }
}

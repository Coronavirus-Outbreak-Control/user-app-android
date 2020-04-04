package com.example.coronavirusherdimmunity.introduction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.coronavirusherdimmunity.MainActivity;
import com.example.coronavirusherdimmunity.PreferenceManager;
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

        writeText();

        Button button_next = findViewById(R.id.button_next);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void writeText() {
        String first = getResources().getString(R.string.we_need_you_notif);
        String second = getResources().getString(R.string.by_clicking);
        String pink = getResources().getString(R.string.tos);

        TextView t = (TextView) findViewById(R.id.we_need_you_notif);

        t.setText(Html.fromHtml(first + "<br/><br/>" + second +
                "<font color='#FF6F61'>" +
                "<u>" + pink + "</u></font>"));
    }
}

package com.example.coronavirusherdimmunity.introduction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.coronavirusherdimmunity.R;

import androidx.appcompat.app.AppCompatActivity;

public class DistanceLogActivity extends AppCompatActivity {

        private Bundle bundle;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //Remove title bar
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //set content view AFTER ABOVE sequence (to avoid crash)
            setContentView(R.layout.intro2b_distance);


            Button button_next, button_skip;

            button_next = findViewById(R.id.button_next);
            button_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    give_StoreLocationPermission();
                    go_nextActivity();
                }
            });

            button_skip = findViewById(R.id.button_skip);
            button_skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DistanceLogActivity.this, NotificationsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

        }
        private void give_StoreLocationPermission() {

            // TODO
        }

        private void go_nextActivity() {

            Intent intent = new Intent(DistanceLogActivity.this, NotificationsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
     }
}

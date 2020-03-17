package com.example.coronavirusherdimmunity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.example.coronavirusherdimmunity.introduction.WelcomeActivity;
import androidx.appcompat.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {

    private PreferenceManager prefManager;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.splashscreen);

        handler=new Handler();

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);


        if (!prefManager.isFirstTimeLaunch()) {
            this.launchHomeScreen();
        }

        this.launchIntroduction();
    }

    private void launchHomeScreen() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);

    }
    private void launchIntroduction() {

        this.prefManager.setFirstTimeLaunch(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);

    }

    @Override
    public void onBackPressed(){
        finish();
    }

}
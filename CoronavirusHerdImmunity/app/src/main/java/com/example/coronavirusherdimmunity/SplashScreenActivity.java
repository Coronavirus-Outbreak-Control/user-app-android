package com.example.coronavirusherdimmunity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.coronavirusherdimmunity.introduction.WelcomeActivity;
import androidx.appcompat.app.AppCompatActivity;


public class SplashScreenActivity extends AppCompatActivity {

    private PreferenceManager prefManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.splashscreen);
        this.writeTitle();

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();

        } else { //if it is first time launch

            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                    finish();
                }
            },3000);
        }
    }

    private void launchHomeScreen() {
        Handler handler=new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        },1500);
    }

    @Override
    public void onBackPressed() { } // disable back button



    private void writeTitle() {
        String pink = getResources().getString(R.string.splash_title_first);
        String next = getResources().getString(R.string.splash_title_next);
        TextView t = (TextView) findViewById(R.id.intro_title);
        t.setText(Html.fromHtml("<font color='#FF6F61'>"+pink+"</font>" + next));
    }
}
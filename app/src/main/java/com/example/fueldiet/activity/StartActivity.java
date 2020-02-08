package com.example.fueldiet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.example.fueldiet.R;

public class StartActivity extends BaseActivity {

    Handler handler = null;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initViews();

        initHandler();
    }

    void initHandler(){
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startIconAnimation();
            }
        }, 100);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, 1200);
    }

    void initViews(){
        logo = findViewById(R.id.logo);
    }

    void startIconAnimation(){
        logo.animate()
                .scaleXBy(1f)
                .scaleYBy(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(1000L)
                .start();
    }

    /**
     * Loads MainActivity
     */
    void startMainActivity(){
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        //So app doesn't start if user cancels the animation
        handler.removeCallbacksAndMessages(null);
        super.onBackPressed();
        super.onPause();
    }
}

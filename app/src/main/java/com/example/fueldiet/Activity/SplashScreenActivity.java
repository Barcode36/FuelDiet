package com.example.fueldiet.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.fueldiet.R;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen splashScreen = new EasySplashScreen(SplashScreenActivity.this);
        splashScreen.withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(getColor(R.color.colorPrimary))
                .withLogo(R.mipmap.ic_launcher_round);

        View easySplashScreen = splashScreen.create();
        setContentView(easySplashScreen);
    }
}

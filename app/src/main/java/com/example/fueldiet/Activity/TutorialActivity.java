package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.viewpager.widget.ViewPager;

import com.example.fueldiet.Adapter.TutorialPagerAdapter;
import com.example.fueldiet.R;
import com.google.android.material.tabs.TabLayout;

public class TutorialActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Welcome to FuelDiet");

        ViewPager viewPager = findViewById(R.id.tutorial_view);
        TutorialPagerAdapter sectionsPagerAdapter = new TutorialPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tutorial_tabs);
        tabs.setupWithViewPager(viewPager, true);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("first", false))
            tabs.getTabAt(0).select();

    }

    @Override
    public void onBackPressed() {
        //nothing, you must finish tutorial
    }
}

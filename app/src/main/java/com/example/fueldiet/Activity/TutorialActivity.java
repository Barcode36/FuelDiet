package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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
        actionBar.setTitle(getString(R.string.welcome_text));

        ViewPager viewPager = findViewById(R.id.tutorial_view);
        TutorialPagerAdapter sectionsPagerAdapter = new TutorialPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tutorial_tabs);
        tabs.setupWithViewPager(viewPager, true);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("first", false))
            tabs.getTabAt(0).select();

        ImageButton next = findViewById(R.id.tutorial_next);
        ImageButton back = findViewById(R.id.tutorial_back);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = tabs.getSelectedTabPosition();
                if (c+1 < 8)
                    tabs.getTabAt(c+1).select();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = tabs.getSelectedTabPosition();
                if (c-1 > -1)
                    tabs.getTabAt(c-1).select();
            }
        });

    }

    @Override
    public void onBackPressed() {
        //nothing, you must finish tutorial
    }
}

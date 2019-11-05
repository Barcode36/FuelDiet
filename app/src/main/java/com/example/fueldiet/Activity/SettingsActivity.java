package com.example.fueldiet.Activity;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.example.fueldiet.Fragment.SettingsFragment;
import com.example.fueldiet.R;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
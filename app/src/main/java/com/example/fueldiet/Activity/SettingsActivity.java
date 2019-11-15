package com.example.fueldiet.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.fueldiet.Fragment.SettingsFragment;
import com.example.fueldiet.R;

import java.util.Locale;

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

        prefs();
    }

    private void prefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void resetTutorial(View v) {
        Log.i("Setting Activity", "reset tutorial");
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showTutorial", true);
        editor.apply();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("selected_unit")){
                Log.i("SHARED-PREFERENCES", "selected_unit changed to: " + sharedPreferences.getString(key, null));
            } else if (key.equals("enable_language")) {
                if (!sharedPreferences.getBoolean(key, false)) {
                    Log.i("SHARED-PREFERENCES", "enable_language is FALSE");
                    String langCode =  getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
                    Log.i("SHARED-PREFERENCES", "new locale will be " + langCode);
                    if ("sl".equals(langCode)) {
                        sharedPreferences.edit().putString("language_select", "slovene").apply();
                    } else {
                        sharedPreferences.edit().putString("language_select", "english").apply();
                    }
                    Log.i("SHARED-PREFERENCES", "new locale is set to " + sharedPreferences.getString("language_select", null));
                    showMessage();
                }
                Log.i("SHARED-PREFERENCES", "enable_language changed to: " + sharedPreferences.getBoolean(key, false));
            } else if (key.equals("language_select")) {
                Log.i("SHARED-PREFERENCES", "language_select changed to: " + sharedPreferences.getString(key, null));
                if (sharedPreferences.getBoolean("enable_language", false)) {
                    String localSelected = sharedPreferences.getString("language_select", "english");
                    Locale locale;
                    if ("slovene".equals(localSelected)) {
                        locale = new Locale("sl", "SI");
                    } else {
                        locale = new Locale("en", "GB");
                    }
                    Resources resources = getResources();
                    Configuration configuration = resources.getConfiguration();
                    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                    configuration.setLocale(locale);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        getApplicationContext().createConfigurationContext(configuration);
                    } else {
                        resources.updateConfiguration(configuration,displayMetrics);
                    }
                    showMessage();
                }
            } else if (key.equals("reset_tutorial")) {
                //reset tutorial prefs
                Log.i("Reset tutorial", "Tutorial will be shown on next load.");
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("showTutorial", true);
                editor.apply();
            }
        }
    };

    private void showMessage() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.restart_required))
                .setCancelable(false)
                .setMessage(getString(R.string.reload_details_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getApplication(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getApplication().startActivity(intent);
                        finish();
                    }
                })
                .create()
                .show();
    }
}
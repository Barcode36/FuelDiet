package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.fragment.SettingsFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingsActivity extends BaseActivity {
    private static final String TAG = "SettingsActivity";
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

    /**
     * Init Shared Preferences
     */
    private void prefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    /**
     * Listener to check for changes in shared preferences and act accordingly
     */
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
        switch (key) {
            case "selected_unit":
                Log.d(TAG, "Listener: selected_unit " + sharedPreferences.getString(key, null));
                break;
            case "enable_language":
                if (!sharedPreferences.getBoolean(key, false)) {
                    Log.d(TAG, "Listener: enable_language is FALSE");
                    String langCode =  getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
                    Log.d(TAG, "Listener: enable_language new locale will be " + langCode);
                    if ("sl".equals(langCode)) {
                        sharedPreferences.edit().putString("language_select", "slovene").apply();
                    } else {
                        sharedPreferences.edit().putString("language_select", "english").apply();
                    }
                    Log.d(TAG, "Listener: enable_language new locale is set to " + sharedPreferences.getString("language_select", null));
                    showMessage();
                }
                Log.d(TAG, "Listener: enable_language languages changed to " +  sharedPreferences.getBoolean(key, false));
                break;
            case "language_select":
                Log.d(TAG, "Listener: language_select changed to: " + sharedPreferences.getString(key, null));
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
                    getApplicationContext().createConfigurationContext(configuration);
                    showMessage();
                }
                break;
            case "selected_vehicle":
                Log.d(TAG, "Listener: selected_vehicle");
                String selected = sharedPreferences.getString("selected_vehicle", null);
                break;
            case "country_select":
                Log.d(TAG, "default country changed to: "+sharedPreferences.getString(key, null));

                //showMessage();

        }
    };

    /**
     * Show alert that restart is required
     */
    private void showMessage() {
        //reset is required after language change
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.restart_required))
                .setCancelable(false)
                .setMessage(getString(R.string.reload_details_message))
                .setPositiveButton("OK", (dialog, id) -> {
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getApplication().startActivity(intent);
                    finish();
                })
                .create()
                .show();
    }
}
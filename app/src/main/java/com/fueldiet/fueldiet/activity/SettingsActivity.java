package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.fragment.SettingsFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        getApplicationContext().createConfigurationContext(configuration);
                    } else {
                        resources.updateConfiguration(configuration,displayMetrics);
                    }
                    showMessage();
                }
                break;
            case "selected_vehicle":
                Log.d(TAG, "Listener: selected_vehicle");
                String selected = sharedPreferences.getString("selected_vehicle", null);
                if (selected != null) {
                    setDefaultVehicle(Long.parseLong(selected));
                }
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

    private void setDefaultVehicle(long id) {
        Log.d(TAG, "setDefaultVehicle: changing default vehicle shortcuts");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainIntent.setAction(Intent.ACTION_VIEW);

            Intent vehicleDetails0 = new Intent(getApplicationContext(), VehicleDetailsActivity.class);
            vehicleDetails0.putExtra("vehicle_id", id);
            vehicleDetails0.putExtra("frag", 0);
            vehicleDetails0.setAction(Intent.ACTION_VIEW);

            Intent vehicleDetails1 = (Intent)vehicleDetails0.clone();
            vehicleDetails1.putExtra("frag", 1);
            Intent vehicleDetails2 = (Intent)vehicleDetails0.clone();
            vehicleDetails1.putExtra("frag", 2);

            Intent addNewFuel = new Intent(getApplicationContext(), AddNewDriveActivity.class);
            addNewFuel.setAction(Intent.ACTION_VIEW);
            addNewFuel.putExtra("vehicle_id", id);
            Intent addNewCost = new Intent(getApplicationContext(), AddNewCostActivity.class);
            addNewCost.setAction(Intent.ACTION_VIEW);
            addNewCost.putExtra("vehicle_id", id);
            Intent addNewReminder = new Intent(getApplicationContext(), AddNewReminderActivity.class);
            addNewReminder.setAction(Intent.ACTION_VIEW);
            addNewReminder.putExtra("vehicle_id", id);

            ShortcutInfo newFuel = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_fuel_add")
                    .setShortLabel(getString(R.string.log_fuel))
                    .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_local_gas_station_shortcut_24px))
                    .setIntents(new Intent[]{
                            mainIntent, vehicleDetails0, addNewFuel
                    })
                    .build();
            ShortcutInfo newCost = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_cost_add")
                    .setShortLabel(getString(R.string.log_cost))
                    .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_euro_symbol_shortcut_24px))
                    .setIntents(new Intent[]{
                            mainIntent, vehicleDetails1, addNewCost
                    })
                    .build();
            ShortcutInfo newReminder = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_reminder_add")
                    .setShortLabel(getString(R.string.add_rem))
                    .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_notifications_shortcut_24px))
                    .setIntents(new Intent[]{
                            mainIntent, vehicleDetails2, addNewReminder
                    })
                    .build();

            if (getSystemService(ShortcutManager.class).getDynamicShortcuts().size() == 1)
                getSystemService(ShortcutManager.class).addDynamicShortcuts(Arrays.asList(newFuel, newCost, newReminder));
            else
                getSystemService(ShortcutManager.class).updateShortcuts(Arrays.asList(newFuel, newCost, newReminder));
        }
    }
}
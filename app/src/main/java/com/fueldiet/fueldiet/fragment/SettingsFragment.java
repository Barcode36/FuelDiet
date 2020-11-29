package com.fueldiet.fueldiet.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.VehicleObject;

import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.fueldiet.fueldiet.activity.MainActivity.PERMISSIONS_STORAGE;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        updateDefaultLang();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                !EasyPermissions.hasPermissions(getContext(), PERMISSIONS_STORAGE)) {
            findPreference("auto_backup").setEnabled(false);
            prefs.edit().putBoolean("auto_backup", false).apply();
        }

        FuelDietDBHelper dbHelper = FuelDietDBHelper.getInstance(getContext());

        Log.d(TAG, "onCreatePreferences: " + prefs.getString("selected_vehicle", null));
        ListPreference selectedVehicle = findPreference("selected_vehicle");

        List<VehicleObject> vehicles = dbHelper.getAllVehicles();
        if (vehicles != null) {
            CharSequence[] vehicleEntry = new CharSequence[vehicles.size()];
            CharSequence[] vehicleValues = new CharSequence[vehicles.size()];
            for (int i = 0; i < vehicles.size(); i++) {
                vehicleEntry[i] = vehicles.get(i).getMake() + " " + vehicles.get(i).getModel();
                vehicleValues[i] = String.valueOf(vehicles.get(i).getId());
            }
            selectedVehicle.setEntries(vehicleEntry);
            selectedVehicle.setEntryValues(vehicleValues);
        } else {
            CharSequence[] vehicleEntry = new CharSequence[0];
            CharSequence[] vehicleValues = new CharSequence[0];
            selectedVehicle.setEntries(vehicleEntry);
            selectedVehicle.setEntryValues(vehicleValues);
        }

        String p_station = prefs.getString("default_petrol_station", "Other");
        ListPreference petrolStation = findPreference("default_petrol_station");

        List<PetrolStationObject> tmp = dbHelper.getAllPetrolStations();
        tmp.sort(new Comparator<PetrolStationObject>() {
            @Override
            public int compare(PetrolStationObject o1, PetrolStationObject o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        CharSequence[] values = new CharSequence[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            values[i] = tmp.get(i).getName();
        }
        petrolStation.setEntries(values);
        petrolStation.setEntryValues(values);
        petrolStation.setValue(p_station);

        String km_mode = prefs.getString("default_km_mode", "odo");
        if (km_mode.equals("odo")) {
            findPreference("default_km_mode").setDefaultValue(getString(R.string.total_meter));
        } else {
            findPreference("default_km_mode").setDefaultValue(getString(R.string.trip_meter));
        }
    }

    private void updateDefaultLang() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        boolean overrideLang = prefs.getBoolean("enable_language", false);
        if (!overrideLang) {
            String langCode = getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
            ListPreference choseLang = findPreference("language_select");
            if ("sl".equals(langCode)) {
                editor.putString("language_select", "slovene").apply();
                choseLang.setValueIndex(1);
            } else {
                editor.putString("language_select", "english").apply();
                choseLang.setValueIndex(0);
            }
        }
    }
}

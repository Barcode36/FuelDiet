package com.fueldiet.fueldiet.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.PetrolStationObject;

import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import static com.fueldiet.fueldiet.activity.MainActivity.PERMISSIONS_STORAGE;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        updateDefaultLang();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

        if (!EasyPermissions.hasPermissions(getContext(), PERMISSIONS_STORAGE)) {
            findPreference("auto_backup").setEnabled(false);
            prefs.edit().putBoolean("auto_backup", false).apply();
        }

        String vehicleName = prefs.getString("selected_vehicle_name", "No vehicle selected");
        Preference selectedVehicle = findPreference("selected_vehicle");
        selectedVehicle.setTitle(vehicleName);

        String p_station = prefs.getString("default_petrol_station", "Other");
        ListPreference petrolStation = findPreference("default_petrol_station");

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(getContext());
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

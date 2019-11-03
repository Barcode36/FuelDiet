package com.example.fueldiet.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.fueldiet.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private ListPreference units;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        units = getPreferenceManager().findPreference("unit");
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

        Log.e("NOT ERR", ""+prefs.getString("unit", "non"));

        String unit = prefs.getString("unit", "litres_per_km");
        Log.e("NOT ERR", ""+prefs.getString("unit", "non"));

        switch (unit) {
            case "litres_per_km":
                units.setValueIndex(0);
            case "km_per_litre":
                units.setValueIndex(1);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}

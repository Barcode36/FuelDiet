package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.fragment.FuelPricesListFragment;
import com.fueldiet.fueldiet.fragment.FuelPricesMapFragment;
import com.fueldiet.fueldiet.object.StationPricesObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;

public class FuelPricesDetailsActivity extends BaseActivity {

    private static final String TAG = "StationPricesDetailsAct";
    private int showSpecific;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_prices_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Fuel prices");

        Intent intent = getIntent();
        ArrayList<StationPricesObject> data = (ArrayList<StationPricesObject>) intent.getSerializableExtra("data");
        HashMap<Integer, String> names = (HashMap<Integer, String>) intent.getSerializableExtra("names");
        showSpecific = intent.getIntExtra("showSpecific", -1);

        /* connect nav bar to fragment */
        BottomNavigationView bottomNav = findViewById(R.id.station_prices_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.station_prices_map_all:
                    selectedFragment = new FuelPricesMapFragment(data, names, showSpecific);
                    break;
                case R.id.station_prices_list_all:
                    selectedFragment = new FuelPricesListFragment(data, names);
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.view_pager, selectedFragment).commit();
            return true;
        });

        int whichMode = intent.getIntExtra("mode", 0);

        if (whichMode == 0) {
            bottomNav.setSelectedItemId(R.id.station_prices_list_all);
        } else {
            bottomNav.setSelectedItemId(R.id.station_prices_map_all);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
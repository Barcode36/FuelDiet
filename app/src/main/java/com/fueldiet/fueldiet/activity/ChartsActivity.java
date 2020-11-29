package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.BarChartFragment;
import com.fueldiet.fueldiet.fragment.LineChartFragment;
import com.fueldiet.fueldiet.fragment.PieChartFragment;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        Intent intent = getIntent();
        final long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        VehicleObject vo = FuelDietDBHelper.getInstance(this).getVehicle(vehicle_id);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.chart_for) + " " + vo.getMake() + " " + vo.getModel());


        BottomNavigationView bottomNav = findViewById(R.id.chart_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFrag;

            switch (item.getItemId()) {
                case R.id.chart_line:
                    selectedFrag = LineChartFragment.newInstance(vehicle_id);
                    break;
                case R.id.chart_bar:
                    selectedFrag = BarChartFragment.newInstance(vehicle_id, vo);
                    break;
                default:
                    //is pie
                    selectedFrag = PieChartFragment.newInstance(vehicle_id);
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.chart_fragment_container, selectedFrag).commit();
            return true;
        });

        bottomNav.setSelectedItemId(R.id.chart_pie);
    }
}

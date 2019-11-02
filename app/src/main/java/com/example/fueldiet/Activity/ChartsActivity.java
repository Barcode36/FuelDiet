package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.Fragment.BarChartFragment;
import com.example.fueldiet.Fragment.LineChartFragment;
import com.example.fueldiet.Fragment.PieChartFragment;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        Intent intent = getIntent();
        final long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        VehicleObject vo = new FuelDietDBHelper(this).getVehicle(vehicle_id);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Charts for " + vo.getMake() + " " + vo.getModel());


        BottomNavigationView bottomNav = findViewById(R.id.chart_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFrag;

            switch (item.getItemId()) {
                case R.id.chart_line:
                    selectedFrag = new LineChartFragment();
                    break;
                case R.id.chart_bar:
                    selectedFrag = new BarChartFragment();
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
        //getSupportFragmentManager().beginTransaction().replace(R.id.chart_fragment_container, new PieChartFragment()).commit();
    }
}

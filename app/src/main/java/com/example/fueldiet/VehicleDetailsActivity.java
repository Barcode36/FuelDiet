package com.example.fueldiet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class VehicleDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);
        Intent intent = getIntent();
        long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        setTitle(vehicle_id);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), vehicle_id);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void setTitle(long id) {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        VehicleObject vo = dbHelper.getVehicle(id);
        TextView tv = findViewById(R.id.title);
        tv.setText(vo.getMake() + " " + vo.getModel());
    }
}
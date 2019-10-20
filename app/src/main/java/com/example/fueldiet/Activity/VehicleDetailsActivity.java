package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Adapter.SectionsPagerAdapter;
import com.example.fueldiet.Object.VehicleObject;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

public class VehicleDetailsActivity extends AppCompatActivity {

    public long vehicle_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);
        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        setTitle();
        ViewPager viewPager = findViewById(R.id.view_pager);
        int frag = intent.getIntExtra("frag", -1);
        if (frag > -1)
            viewPager.setCurrentItem(frag);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), vehicle_id);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void setTitle() {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        VehicleObject vo = dbHelper.getVehicle(vehicle_id);
        TextView tv = findViewById(R.id.title);
        tv.setText(vo.getMake() + " " + vo.getModel());
    }
}
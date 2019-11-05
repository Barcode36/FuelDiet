package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Adapter.SectionsPagerAdapter;
import com.example.fueldiet.Object.VehicleObject;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

public class VehicleDetailsActivity extends BaseActivity {

    public long vehicle_id;
    private ImageButton chart_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_details);
        Intent intent = getIntent();
        int maybeReminderID = intent.getIntExtra("reminder_id", -2);
        if (maybeReminderID != -2) {
            NotificationManagerCompat.from(getApplicationContext()).cancel(maybeReminderID);
        }
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        setTitle();
        ViewPager viewPager = findViewById(R.id.view_pager);
        int frag = intent.getIntExtra("frag", -1);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), vehicle_id);
        viewPager.setAdapter(sectionsPagerAdapter);
        chart_button = findViewById(R.id.vehicle_details_chart_img);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        if (frag > -1)
            tabs.getTabAt(frag).select();

        chart_button.setOnClickListener(v -> {
            Intent intent1 = new Intent(VehicleDetailsActivity.this, ChartsActivity.class);
            intent1.putExtra("vehicle_id", vehicle_id);
            startActivity(intent1);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(VehicleDetailsActivity.this, MainActivity.class));
    }

    private void setTitle() {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        VehicleObject vo = dbHelper.getVehicle(vehicle_id);
        TextView tv = findViewById(R.id.title);
        tv.setText(vo.getMake() + " " + vo.getModel());
    }
}
package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.adapter.VehicleSelectAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.VehicleObject;

import java.util.ArrayList;
import java.util.List;

public class SelectVehicleActivity extends AppCompatActivity {

    private static final String TAG = "SelectVehicleActivity";

    RecyclerView recyclerView;
    VehicleSelectAdapter adapter;
    FuelDietDBHelper dbHelper;
    List<VehicleObject> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vehicle);

        Intent intent = getIntent();
        Log.d(TAG, "onCreate: " + intent.getStringExtra("operation"));
        String operation = intent.getStringExtra("operation");

        getSupportActionBar().setTitle(R.string.select_vehicle);

        data = new ArrayList<>();

        dbHelper = FuelDietDBHelper.getInstance(this);
        fillData();

        recyclerView = findViewById(R.id.select_vehicle_recycler);
        adapter = new VehicleSelectAdapter(this, data);
        adapter.setOnItemClickListener(element_id -> {
            if (operation.equals("addFuelLog")) {
                Intent addNewFuel = new Intent(getApplicationContext(), AddNewDriveActivity.class);
                addNewFuel.setAction("open");
                addNewFuel.putExtra("vehicle_id", element_id);
                startActivity(addNewFuel);
            } else if (operation.equals("addCost")) {
                Intent addNewCost = new Intent(getApplicationContext(), AddNewCostActivity.class);
                addNewCost.setAction("open");
                addNewCost.putExtra("vehicle_id", element_id);
                startActivity(addNewCost);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fillData() {
        data.clear();
        data.addAll(dbHelper.getAllVehicles());
    }
}

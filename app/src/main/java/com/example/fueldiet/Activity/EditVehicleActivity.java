package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fueldiet.BaseActivity;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Object.VehicleObject;

public class EditVehicleActivity extends BaseActivity {

    long vehicleID;
    AutoCompleteTextView editMake;
    EditText editModel;
    EditText editFuel;
    EditText editEngine;
    EditText editHP;
    EditText editTransmission;
    FuelDietDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vehicle);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_vehicle_title);


        displayValues();

        findViewById(R.id.edit_vehicle_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEdit();
            }
        });
    }



    private void displayValues() {
         editMake = findViewById(R.id.edit_vehicle_make_autocomplete);
         editModel = findViewById(R.id.edit_vehicle_model_input);
         editEngine = findViewById(R.id.edit_vehicle_engine_input);
         editFuel = findViewById(R.id.edit_vehicle_fuel_input);
         editHP = findViewById(R.id.edit_vehicle_hp_input);
         editTransmission = findViewById(R.id.edit_vehicle_transmission_input);

         VehicleObject vo = dbHelper.getVehicle(vehicleID);

         editMake.setText(vo.getMake());
         editModel.setText(vo.getModel());
         editTransmission.setText(vo.getTransmission());
         editEngine.setText(vo.getEngine());
         editFuel.setText(vo.getFuel());
         editHP.setText(vo.getHp()+"", TextView.BufferType.EDITABLE);
    }

    private void saveEdit() {
        VehicleObject vo = new VehicleObject();
        vo.setId(vehicleID);
        boolean ok = true;
        ok = ok && vo.setMake(editMake.getText().toString());
        ok = ok && vo.setModel(editModel.getText().toString());
        ok = ok && vo.setTransmission(editTransmission.getText().toString());
        ok = ok && vo.setEngine(editEngine.getText().toString());
        ok = ok && vo.setFuel(editFuel.getText().toString());
        ok = ok && vo.setHp(editHP.getText().toString());

        if (!ok) {
            Toast.makeText(this, "Please insert text in all of the fields", Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.updateVehicle(vo);

        startActivity(new Intent(EditVehicleActivity.this, MainActivity.class));
    }
}

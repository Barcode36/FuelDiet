package com.example.fueldiet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditVehicleActivity extends AppCompatActivity {

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
        actionBar.setTitle("Edit " + dbHelper.getVehicle(vehicleID).getModel());


        displayValues();

        findViewById(R.id.save_vehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEdit();
            }
        });
    }



    private void displayValues() {
         editMake = findViewById(R.id.edit_make);
         editModel = findViewById(R.id.edit_model);
         editEngine = findViewById(R.id.edit_engine);
         editFuel = findViewById(R.id.edit_fuel);
         editHP = findViewById(R.id.edit_hp);
         editTransmission = findViewById(R.id.edit_transmission);

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

package com.example.fueldiet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class ActivityAddNewVehicle extends AppCompatActivity {

    private SQLiteDatabase mDatabase;
    private EditText make;
    private EditText model;
    private EditText fuel;
    private EditText engine;
    private EditText transmission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create new vehicle");

        make = findViewById(R.id.editText_make);
        model = findViewById(R.id.editText_model);
        fuel = findViewById(R.id.editText_fuel);
        engine = findViewById(R.id.editText_engine);
        transmission = findViewById(R.id.editText_transmission);

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        FloatingActionButton addVehicle = findViewById(R.id.save_vehicle);

        addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewVehicle();
            }
        });
    }

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save vehicle - floating button");

        String makeText = toCapitalCaseWords(make.getText().toString().trim());
        String modelText = toCapitalCaseWords(model.getText().toString().trim());
        String engineText = engine.getText().toString().trim();
        String fuelText = toCapitalCaseWords(fuel.getText().toString().trim());
        String transmissionText = toCapitalCaseWords(transmission.getText().toString().trim());

        if (makeText.length() == 0 || modelText.length() == 0 || engineText.length() == 0 ||
                fuelText.length() == 0 || transmissionText.length() == 0) {
            Log.i("FIELD/S EMPTY", "One or more fields are empty");
            Toast.makeText(this, "Please insert text in all of the fields", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, makeText);
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, modelText);
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, engineText);
        cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, fuelText);
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, transmissionText);

        mDatabase.insert(FuelDietContract.VehicleEntry.TABLE_NAME, null, cv);

        startActivity(new Intent(ActivityAddNewVehicle.this, MainActivity.class));
    }
}

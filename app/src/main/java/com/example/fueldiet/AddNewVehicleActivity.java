package com.example.fueldiet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddNewVehicleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FuelDietDBHelper dbHelper;
    private AutoCompleteTextView make;
    private EditText model;
    private Spinner fuel;
    private String fuelSelected;
    private EditText engine;
    private EditText hp;
    private EditText transmission;
    public String[] manufacturers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_vehicle_title);

        make = findViewById(R.id.add_vehicle_make_autocomplete);
        model = findViewById(R.id.add_vehicle_model_input);
        fuel = findViewById(R.id.add_vehicle_fuel_spinner);
        engine = findViewById(R.id.add_vehicle_engine_input);
        hp = findViewById(R.id.add_vehicle_hp_input);
        transmission = findViewById(R.id.add_vehicle_transmission_input);

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuel.setAdapter(adapterS);
        fuel.setOnItemSelectedListener(this);

        dbHelper = new FuelDietDBHelper(this);

        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> addNewVehicle());



        manufacturers = MainActivity.manufacturers.keySet().stream().toArray(String[]::new);
        //for (int u = 0; u < MainActivity.manufacturers.size(); u++)
        //    manufacturers[u] = MainActivity.manufacturers.get(u).getName();

        Log.i("SIZE", manufacturers.length+"");

        AutoCompleteTextView editText = findViewById(R.id.add_vehicle_make_autocomplete);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, manufacturers);
        editText.setAdapter(adapter);
    }

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save vehicle_template - floating button");

        boolean ok = true;

        VehicleObject vo = new VehicleObject();
        ok = ok && vo.setHp(hp.getText().toString());
        ok = ok && vo.setFuel(fuelSelected);
        ok = ok && vo.setEngine(engine.getText().toString());
        ok = ok && vo.setTransmission(transmission.getText().toString());
        ok = ok && vo.setMake(make.getText().toString());
        ok = ok && vo.setModel(model.getText().toString());

        if (!ok) {
            Toast.makeText(this, "Please insert text in all of the fields", Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.addVehicle(vo);

        startActivity(new Intent(AddNewVehicleActivity.this, MainActivity.class));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fuelSelected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        fuelSelected = null;
    }
}

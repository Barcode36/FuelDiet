package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fueldiet.Adapter.AutoCompleteManufacturerAdapter;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class AddNewVehicleActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private FuelDietDBHelper dbHelper;
    private AppCompatAutoCompleteTextView make;
    private TextInputLayout model;
    private Spinner fuel;
    private String fuelSelected;
    private TextInputLayout engine;
    private TextInputLayout hp;
    private TextInputLayout initKM;
    private TextInputLayout transmission;
    public List<ManufacturerObject> manufacturers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_vehicle_title);

        make = findViewById(R.id.add_vehicle_make_autocomplete);
        model = findViewById(R.id.add_vehicle_model_input);
        fuel = findViewById(R.id.add_vehicle_fuel_type_spinner);
        engine = findViewById(R.id.add_vehicle_engine_input);
        hp = findViewById(R.id.add_vehicle_hp_input);
        initKM = findViewById(R.id.add_vehicle_start_km_input);
        transmission = findViewById(R.id.add_vehicle_transmission_input);

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuel.setAdapter(adapterS);
        fuel.setOnItemSelectedListener(this);

        dbHelper = new FuelDietDBHelper(this);

        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> addNewVehicle());

        //manufacturers = MainActivity.manufacturers.keySet().stream().toArray(String[]::new);
        manufacturers = new ArrayList<>(MainActivity.manufacturers.values());
        //for (int u = 0; u < MainActivity.manufacturers.size(); u++)
        //    manufacturers[u] = MainActivity.manufacturers.get(u).getName();

        AutoCompleteTextView editText = findViewById(R.id.add_vehicle_make_autocomplete);
        /*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, manufacturers);
        editText.setAdapter(adapter);*/
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        editText.setAdapter(adapter);
    }

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save card_template_vehicle - floating button");

        boolean ok = true;
        VehicleObject vo = new VehicleObject();
        if (initKM.getEditText().getText().toString().equals(""))
            ok = ok && vo.setInitKM(0);
        else
            ok = ok && vo.setInitKM(initKM.getEditText().getText().toString());

        ok = ok && vo.setHp(hp.getEditText().getText().toString());
        ok = ok && vo.setFuel(fuelSelected);
        ok = ok && vo.setEngine(engine.getEditText().getText().toString());
        ok = ok && vo.setTransmission(transmission.getEditText().getText().toString());
        ok = ok && vo.setMake(make.getText().toString());
        ok = ok && vo.setModel(model.getEditText().getText().toString());

        if (!ok) {
            Toast.makeText(this, getString(R.string.fill_text_edits), Toast.LENGTH_LONG).show();
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

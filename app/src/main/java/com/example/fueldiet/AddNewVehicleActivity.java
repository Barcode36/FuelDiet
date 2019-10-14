package com.example.fueldiet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class AddNewVehicleActivity extends AppCompatActivity {

    private FuelDietDBHelper dbHelper;
    private EditText make;
    private EditText model;
    private EditText fuel;
    private EditText engine;
    private EditText hp;
    private EditText transmission;
    public String[] manufacturers;

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
        hp = findViewById(R.id.editText_hp);
        transmission = findViewById(R.id.editText_transmission);

        dbHelper = new FuelDietDBHelper(this);

        FloatingActionButton addVehicle = findViewById(R.id.save_vehicle);
        addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewVehicle();
            }
        });


        String response = loadJSONFromAsset();
        List<ManufacturerObject> items = new Gson().fromJson(response.toString(), new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = new String[items.size()];

        for (int u = 0; u < items.size(); u++)
            manufacturers[u] = items.get(u).getName();

        Log.i("SIZE", manufacturers.length+"");

        AutoCompleteTextView editText = findViewById(R.id.editText_make);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, manufacturers);
        editText.setAdapter(adapter);
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.carlogos);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save vehicle - floating button");

        boolean ok = true;

        VehicleObject vo = new VehicleObject();
        ok = ok && vo.setmHp(hp.getText().toString());
        ok = ok && vo.setmFuel(fuel.getText().toString());
        ok = ok && vo.setmEngine(engine.getText().toString());
        ok = ok && vo.setmTransmission(transmission.getText().toString());
        ok = ok && vo.setmBrand(make.getText().toString());
        ok = ok && vo.setmModel(make.getText().toString());

        if (!ok) {
            Toast.makeText(this, "Please insert text in all of the fields", Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.addVehicle(vo);

        startActivity(new Intent(AddNewVehicleActivity.this, MainActivity.class));
    }
}

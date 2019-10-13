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

public class ActivityAddNewVehicle extends AppCompatActivity {

    private SQLiteDatabase mDatabase;
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

        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        FloatingActionButton addVehicle = findViewById(R.id.save_vehicle);

        addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewVehicle();
            }
        });

        //jsonParse();
        //String[] manufacturers = man.toArray(new String[0]);
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
/*
    private void jsonParse() {

        String url = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/car-logos.json";
        man = new ArrayList<>();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
        JsonArrayRequest request = new JsonArrayRequest(, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject tmp = response.getJSONObject(i);
                                String name = tmp.getString("name");
                                Log.i("JSON", name);
                                //String fileName = tmp.getString("fileName");

                                man.add(name);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

 */

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save vehicle - floating button");

        String makeText = toCapitalCaseWords(make.getText().toString().trim());
        String modelText = toCapitalCaseWords(model.getText().toString().trim());
        String engineText = engine.getText().toString().trim();
        String fuelText = toCapitalCaseWords(fuel.getText().toString().trim());
        String hptext = toCapitalCaseWords(hp.getText().toString().trim());
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
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, fuelText);
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, transmissionText);

        mDatabase.insert(FuelDietContract.VehicleEntry.TABLE_NAME, null, cv);

        startActivity(new Intent(ActivityAddNewVehicle.this, MainActivity.class));
    }
}

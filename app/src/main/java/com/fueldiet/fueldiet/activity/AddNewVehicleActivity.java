package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.fueldiet.fueldiet.adapter.AutoCompleteManufacturerAdapter;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddNewVehicleActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FuelDietDBHelper dbHelper;
    private AutoCompleteTextView make;
    private TextInputLayout model;
    private Spinner fuel;
    private String fuelSelected;
    private TextInputLayout engine;
    private TextInputLayout hp;
    private TextInputLayout initKM;
    private TextInputLayout transmission;
    private ImageView logoImg;
    private TextInputLayout logoText;
    public List<ManufacturerObject> manufacturers;
    private ImageView clearImg;

    private Uri customImage;
    private String fileName;

    /**
     * If vehicle is not saved, delete custom image
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (customImage != null) {
            try {
                File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                File img = new File(storageDIR, fileName);
                img.delete();
            } catch (Exception e) {
                Log.e("AddNewVehicleActivity - Back", "Custom image was not found");
            }
        }
    }

    /**
     * If vehicle is not saved, delete custom image
     * @param item if back button
     * @return back activity
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (customImage != null) {
                try {
                    File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                    File img = new File(storageDIR, fileName);
                    img.delete();
                } catch (Exception e) {
                    Log.e("AddNewVehicleActivity - BarBack", "Custom image was not found");
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_vehicle_title);
        dbHelper = new FuelDietDBHelper(this);

        initVariables();
        fillWithData();

        make.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                changeImage();
        });
        logoText.getEditText().setOnClickListener(v -> showImagePicker());
        clearImg.setOnClickListener(v -> clearCustomImg());

        /*
         * Save button
         */
        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> addNewVehicle());
    }

    /**
     * Link fields with variables
     */
    private void initVariables() {
        make = findViewById(R.id.add_vehicle_make_autocomplete);
        model = findViewById(R.id.add_vehicle_model_input);
        fuel = findViewById(R.id.add_vehicle_fuel_type_spinner);
        engine = findViewById(R.id.add_vehicle_engine_input);
        hp = findViewById(R.id.add_vehicle_hp_input);
        initKM = findViewById(R.id.add_vehicle_start_km_input);
        transmission = findViewById(R.id.add_vehicle_transmission_input);
        logoImg = findViewById(R.id.add_vehicle_make_logo_img);
        logoText = findViewById(R.id.add_vehicle_make_text);
        clearImg = findViewById(R.id.add_vehicle_clear_custom_img);
        logoImg.setOnClickListener(v -> showImagePicker());
        customImage = null;
    }

    /**
     * Fill fields with Data
     */
    private void fillWithData() {
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuel.setAdapter(adapterS);
        fuel.setOnItemSelectedListener(this);
        manufacturers = new ArrayList<>(MainActivity.manufacturers.values());
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        make.setAdapter(adapter);
    }

    /**
     * Display either the custom image or the predefined.
     */
    private void changeImage() {
        if (customImage != null) {
            Glide.with(getApplicationContext()).load(customImage).into(logoImg);
        } else {
            try {
                ManufacturerObject manufacturerObject = MainActivity.manufacturers.get(make.getText().toString());
                int resourceId = getApplicationContext().getResources().getIdentifier(
                        manufacturerObject.getFileNameModNoType(),
                        "drawable",
                        getApplicationContext().getPackageName()
                );
                Glide.with(getApplicationContext()).load(resourceId).into(logoImg);
            } catch (NullPointerException e) {
                Log.e("AddNewVehicleActivity - "+e.getClass().getSimpleName(), "Make is not found in data ∴ custom make");
                Glide.with(getApplicationContext()).load(R.drawable.ic_help_outline_black_24dp).into(logoImg);
            } catch (Exception oe) {
                Log.e("AddNewVehicleActivity - "+oe.getClass().getSimpleName(), oe.toString());
                Glide.with(getApplicationContext()).load(R.drawable.ic_help_outline_black_24dp).into(logoImg);
            }
        }
    }

    /**
     * Open image picker dialog
     */
    private void showImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            if (customImage != null)
                clearCustomImg();
            customImage = data.getData();
            changeImage();
            clearImg.setVisibility(View.VISIBLE);
            fileName = make.getText().toString() + "_" + Calendar.getInstance().getTimeInMillis()/1000 + ".png";
            Utils.downloadImage(getResources(), getApplicationContext(), customImage, fileName);
        }
    }

    /**
     * Delete custom image from files
     */
    private void clearCustomImg() {
        try {
            File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
            File img = new File(storageDIR, fileName);
            img.delete();
        } catch (Exception e) {
            Log.e("AddNewVehicleActivity - RemoveImg", "Image was not found");
        } finally {
            customImage = null;
            changeImage();
            clearImg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Save this vehicle to db
     */
    private void addNewVehicle() {
        boolean ok = true;
        VehicleObject vo = new VehicleObject();
        if (initKM.getEditText().getText().toString().equals(""))
            vo.setOdoKm(0);
        else
            vo.setOdoKm(initKM.getEditText().getText().toString());

        vo.setCustomImg(fileName);

        ok = ok && vo.setHp(hp.getEditText().getText().toString());
        ok = ok && vo.setFuel(fuelSelected);
        ok = ok && vo.setEngine(engine.getEditText().getText().toString().trim());
        ok = ok && vo.setTransmission(transmission.getEditText().getText().toString().trim());
        ok = ok && vo.setMake(make.getText().toString().trim());
        ok = ok && vo.setModel(model.getEditText().getText().toString().trim());

        if (!ok) {
            Toast.makeText(this, getString(R.string.fill_text_edits), Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.addVehicle(vo);
        finish();
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

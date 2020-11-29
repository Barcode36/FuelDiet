package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.adapter.AutoCompleteManufacturerAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddNewVehicleActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddNewVehicleActivity";
    private FuelDietDBHelper dbHelper;
    private AutoCompleteTextView make, fuel;
    private TextInputLayout model;
    private String fuelSelected;
    private TextInputLayout engine;
    private TextInputLayout hp;
    private TextInputLayout torque;
    private TextInputLayout initKM;
    private TextInputLayout transmission;
    private ImageView logoImg;
    private MaterialButton logoSet, logoDelete;
    public List<ManufacturerObject> manufacturers;

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
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_vehicle_title);
        dbHelper = FuelDietDBHelper.getInstance(this);

        initVariables();
        fillWithData();

        make.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                changeImage();
        });
        logoSet.setOnClickListener(v -> showImagePicker());
        logoDelete.setOnClickListener(v -> clearCustomImg());

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
        torque = findViewById(R.id.add_vehicle_torque_input);
        initKM = findViewById(R.id.add_vehicle_start_km_input);
        transmission = findViewById(R.id.add_vehicle_transmission_input);
        logoImg = findViewById(R.id.add_vehicle_make_logo_img);
        logoSet = findViewById(R.id.add_vehicle_set_img);
        logoDelete = findViewById(R.id.add_vehicle_remove_img);
        logoImg.setOnClickListener(v -> showImagePicker());
        customImage = null;
    }

    /**
     * Fill fields with Data
     */
    private void fillWithData() {
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, R.layout.list_item);
        adapterS.setDropDownViewResource(R.layout.list_item);
        fuel.setAdapter(adapterS);
        fuel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: fuel type set to "+s.toString());
                fuelSelected = s.toString();
            }
        });
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
            logoDelete.setVisibility(View.VISIBLE);
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
            logoDelete.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Save this vehicle to db
     */
    private void addNewVehicle() {
        boolean ok = true;
        VehicleObject vo = new VehicleObject();
        if (initKM.getEditText().getText().toString().equals(""))
            vo.setOdoFuelKm(0);
        else
            vo.setOdoFuelKm(initKM.getEditText().getText().toString());

        vo.setOdoCostKm(vo.getOdoFuelKm());
        vo.setOdoRemindKm(vo.getOdoFuelKm());

        vo.setCustomImg(fileName);

        ok = ok && vo.setHp(hp.getEditText().getText().toString());
        ok = ok && vo.setTorque(torque.getEditText().getText().toString());
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
}

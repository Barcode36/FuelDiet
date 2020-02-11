package com.example.fueldiet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.bumptech.glide.Glide;
import com.example.fueldiet.adapter.AutoCompleteManufacturerAdapter;
import com.example.fueldiet.object.DriveObject;
import com.example.fueldiet.object.ManufacturerObject;
import com.example.fueldiet.object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EditVehicleActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FuelDietDBHelper dbHelper;
    private long vehicleID;
    private AppCompatAutoCompleteTextView make;
    private TextInputLayout model;
    private Spinner fuel;
    private String fuelSelected;
    private TextInputLayout engine;
    private TextInputLayout hp;
    private TextInputLayout initKM;
    private TextInputLayout transmission;
    public List<ManufacturerObject> manufacturers;

    private ImageView logoImg;
    private TextInputLayout logoText;
    private ImageView clearImg;
    private Uri customImage;
    private String fileName;

    private VehicleObject oldVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);
        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_vehicle_title);

        linkFields();
        fillDropDowns();
        enterValues();

        /*
         *Save button
         */
        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> saveEdit());

        make.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                changeImage();
        });
        logoImg.setOnClickListener(v -> showImagePicker());
        logoText.getEditText().setOnClickListener(v -> showImagePicker());
        clearImg.setOnClickListener(v -> clearCustomImg());
    }

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
                Log.e("EditVehicleActivity - Back", "Custom image was not found");
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
                    Log.e("EditVehicleActivity - BarBack", "Custom image was not found");
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.are_you_sure))
                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            //Toast.makeText(this,"LOLOLOL", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create link between fields and variables
     */
    private void linkFields() {
        Log.i("Edit vehicle", "linking text fields");
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
    }

    /**
     * Fill dropdown lists
     */
    private void fillDropDowns() {
        Log.i("Edit vehicle", "filling dropdown with values");
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuel.setAdapter(adapterS);
        fuel.setOnItemSelectedListener(this);
        manufacturers = new ArrayList<>(MainActivity.manufacturers.values());
        Collections.sort(manufacturers, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        make.setAdapter(adapter);
        clearImg.setOnClickListener(v -> clearCustomImg());
    }

    /**
     * Insert vehicle data to fields
     */
    private void enterValues() {
        customImage = null;
        fileName = null;
        Log.i("Edit vehicle", "inserting vehicle values");
        oldVO = dbHelper.getVehicle(vehicleID);
        final String manufacturer = oldVO.getMake();
        make.setText(manufacturer);
        model.getEditText().setText(oldVO.getModel());
        hp.getEditText().setText(oldVO.getHp()+"");
        engine.getEditText().setText(oldVO.getEngine());
        if (oldVO.getOdoKm() != 0)
            initKM.getEditText().setText(oldVO.getOdoKm()+"");
        transmission.getEditText().setText(oldVO.getTransmission());
        fuelSelected = oldVO.getFuel();
        final List<String> fuelValues = Arrays.asList(getResources().getStringArray(R.array.fuel));
        final int fuelPos = fuelValues.indexOf(fuelSelected);
        fuel.setSelection(fuelPos);
        model.requestFocus();
        fileName = oldVO.getCustomImg();
        if (fileName != null) {
            clearImg.setVisibility(View.VISIBLE);
            try {
                File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
                customImage = Uri.fromFile(new File(storageDIR, fileName));
            } catch (Exception e) {
                fileName = null;
                oldVO.setCustomImg(null);
                Log.e("EditVehicleActivity - LoadCustomImg - "+e.getClass().getSimpleName(), "Custom Img was not found, reseting to default");
            }
        }
        changeImage();
    }

    /**
     * Display either the custom image or the predefined one
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
                Log.e("EditVehicleActivity - "+e.getClass().getSimpleName(), "Make is not found in data ∴ custom make");
                Glide.with(getApplicationContext()).load(R.drawable.ic_help_outline_black_24dp).into(logoImg);
            } catch (Exception oe) {
                Log.e("EditVehicleActivity-"+oe.getClass().getSimpleName(), oe.toString());
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
     * Remove custom image
     */
    private void clearCustomImg() {
        try {
            File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
            File img = new File(storageDIR, fileName);
            img.delete();
        } catch (Exception e) {
            Log.e("EditVehicleActivity- RemoveImg", "Image was not found");
        } finally {
            fileName = null;
            customImage = null;
            changeImage();
            clearImg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Save changed vehicle
     */
    private void saveEdit() {
        Log.i("Edit vehicle", "saving changes");
        VehicleObject vo = new VehicleObject();
        vo.setId(vehicleID);
        boolean ok = true;
        if (initKM.getEditText().getText().toString().equals(""))
            vo.setOdoKm(0);
         else
            vo.setOdoKm(initKM.getEditText().getText().toString());

        vo.setCustomImg(fileName);

        ok = ok && vo.setMake(make.getText().toString());
        ok = ok && vo.setModel(model.getEditText().getText().toString());
        ok = ok && vo.setTransmission(transmission.getEditText().getText().toString());
        ok = ok && vo.setEngine(engine.getEditText().getText().toString());
        ok = ok && vo.setFuel(Utils.fromSLOtoENG(fuelSelected));
        ok = ok && vo.setHp(hp.getEditText().getText().toString());

        if (!ok) {
            Toast.makeText(this, getString(R.string.fill_text_edits), Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.updateVehicle(vo);

        if (oldVO.getOdoKm() != vo.getOdoKm()) {
            int diff = vo.getOdoKm() - oldVO.getOdoKm();
            List<DriveObject> oldDrives = dbHelper.getAllDrives(vehicleID);
            for (DriveObject driveObject : oldDrives) {
                int newOdo = driveObject.getOdo() + diff;
                driveObject.setOdo(newOdo);
                dbHelper.updateDriveODO(driveObject);
            }
        }
        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);

        Intent data = new Intent();
        String text = "ok";
        //---set the data to pass back---
        data.setData(Uri.parse(text));
        setResult(RESULT_OK, data);
        //---close the activity---
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.delete_vehicle, menu);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fuelSelected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        fuelSelected = null;
    }

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        //result from yes/no whether to delete
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                //removeItem(vehicleID);
                Intent data = new Intent();
                String text = String.valueOf(vehicleID);
                //---set the data to pass back---
                data.setData(Uri.parse(text));
                setResult(RESULT_OK, data);
                //---close the activity---
                finish();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Toast.makeText(this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                break;
        }
    };
}
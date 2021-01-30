package com.fueldiet.fueldiet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.utils.TextInputValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditVehicleActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditVehicleActivity";
    private long vehicleID;
    private FuelDietDBHelper dbHelper;
    private Locale locale;

    private AutoCompleteTextView makeEdit;
    private AutoCompleteTextView fuelEdit;
    private AutoCompleteTextView hybridEdit;
    private AutoCompleteTextView transmissionEdit;

    private TextInputLayout make;
    private TextInputLayout model;
    private TextInputLayout engine;
    private TextInputLayout hybrid;
    private TextInputLayout fuel;
    private TextInputLayout hp;
    private TextInputLayout torque;
    private TextInputLayout modelYear;
    private TextInputLayout initKm;
    private TextInputLayout transmission;

    private TextInputEditText modelEdit;
    private TextInputEditText modelYearEdit;
    private TextInputEditText engineEdit;
    private TextInputEditText hpEdit;
    private TextInputEditText torqueEdit;

    private TextInputValidator validatorMake;
    private TextInputValidator validatorModel;
    private TextInputValidator validatorFuelType;
    private TextInputValidator validatorHybrid;
    private TextInputValidator validatorEngine;
    private TextInputValidator validatorTransmission;
    private TextInputValidator validatorModelYear;
    private TextInputValidator validatorPower;
    private TextInputValidator validatorTorque;

    private ImageView logoImg;
    private MaterialButton logoSet;
    private MaterialButton logoDelete;

    private Uri customImage;
    private String fileName;

    private VehicleObject oldVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_vehicle_title);

        initVariables();
        initializeDropdowns();
        addValidators();
        addClickListeners();
        enterValues();

        makeEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                changeImage();
        });
        Log.d(TAG, "onCreate: finished");
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

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setMessage(getString(R.string.are_you_sure))
                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Create link between fields and variables
     */
    private void initVariables() {
        Log.d(TAG, "initVariables: started...");
        this.makeEdit = findViewById(R.id.add_vehicle_make_spinner);
        this.make = findViewById(R.id.add_vehicle_make_input);
        this.model = findViewById(R.id.add_vehicle_model_input);
        this.modelEdit = findViewById(R.id.add_vehicle_model_input_edit);
        this.fuel = findViewById(R.id.add_vehicle_fuel_type_input);
        this.fuelEdit = findViewById(R.id.add_vehicle_fuel_type_spinner);
        this.engine = findViewById(R.id.add_vehicle_engine_input);
        this.engineEdit = findViewById(R.id.add_vehicle_engine_input_edit);
        this.hp = findViewById(R.id.add_vehicle_hp_input);
        this.hpEdit = findViewById(R.id.add_vehicle_hp_input_edit);
        this.torque = findViewById(R.id.add_vehicle_torque_input);
        this.torqueEdit = findViewById(R.id.add_vehicle_torque_input_edit);
        this.initKm = findViewById(R.id.add_vehicle_start_km_input);
        this.modelYear = findViewById(R.id.add_vehicle_model_year_input);
        this.modelYearEdit = findViewById(R.id.add_vehicle_model_year_input_edit);
        this.transmission = findViewById(R.id.add_vehicle_transmission_input);
        this.transmissionEdit = findViewById(R.id.add_vehicle_transmission_input_spinner);
        this.hybrid = findViewById(R.id.add_vehicle_hybrid_input);
        this.hybridEdit = findViewById(R.id.add_vehicle_hybrid_spinner);

        this.logoImg = findViewById(R.id.add_vehicle_make_logo_img);
        this.logoSet = findViewById(R.id.add_vehicle_set_img);
        this.logoDelete = findViewById(R.id.add_vehicle_remove_img);
        this.logoImg.setOnClickListener(v -> showImagePicker());
        Log.d(TAG, "initVariables: finished");
    }

    /**
     * Fill dropdown lists
     */
    private void initializeDropdowns() {
        Log.d(TAG, "initializeDropdowns: started...");
        /* fuel types */
        ArrayAdapter<CharSequence> adapterFuel = ArrayAdapter.createFromResource(this,
                R.array.fuel, R.layout.list_item);
        adapterFuel.setDropDownViewResource(R.layout.list_item);
        fuelEdit.setAdapter(adapterFuel);
        /* manufacturers */
        List<ManufacturerObject> manufacturers = new ArrayList<>(MainActivity.manufacturers.values());
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        makeEdit.setAdapter(adapter);
        /* transmissions */
        ArrayAdapter<CharSequence> adapterTransmission = ArrayAdapter.createFromResource(this,
                R.array.transmission, R.layout.list_item);
        adapterTransmission.setDropDownViewResource(R.layout.list_item);
        transmissionEdit.setAdapter(adapterTransmission);
        /* hybrid */
        ArrayAdapter<CharSequence> adapterHybrid = ArrayAdapter.createFromResource(this,
                R.array.hybrid, R.layout.list_item);
        adapterHybrid.setDropDownViewResource(R.layout.list_item);
        hybridEdit.setAdapter(adapterHybrid);
        Log.d(TAG, "initializeDropdowns: finished");
    }

    private void addValidators() {
        Log.d(TAG, "addValidators: started...");
        validatorMake = new TextInputValidator(this, locale, this.make, this.makeEdit);
        validatorModel = new TextInputValidator(this, locale, this.model, this.modelEdit);
        validatorFuelType = new TextInputValidator(this, locale, this.fuel, this.fuelEdit);
        validatorHybrid = new TextInputValidator(this, locale, this.hybrid, this.hybridEdit);
        validatorEngine = new TextInputValidator(this, locale, this.engine, this.engineEdit);
        validatorTransmission = new TextInputValidator(this, locale, this.transmission, this.transmissionEdit);
        validatorModelYear = new TextInputValidator(this, locale, this.modelYear, this.modelYearEdit);
        validatorPower = new TextInputValidator(this, locale, this.hp, this.hpEdit);
        validatorTorque = new TextInputValidator(this, locale, this.torque, this.torqueEdit);
        Log.d(TAG, "addValidators: finished");
    }

    private void addClickListeners() {
        Log.d(TAG, "addClickListeners: started...");
        this.logoImg.setOnClickListener(v -> showImagePicker());
        logoSet.setOnClickListener(v -> showImagePicker());
        logoDelete.setOnClickListener(v -> clearCustomImg());

        /*Save button*/
        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> saveEdit());
        Log.d(TAG, "addClickListeners: finished");
    }

    /**
     * Insert vehicle data to fields
     */
    private void enterValues() {
        Log.d(TAG, "enterValues: started...");
        customImage = null;
        fileName = null;
        oldVO = dbHelper.getVehicle(vehicleID);
        final String manufacturer = oldVO.getMake();
        makeEdit.setText(manufacturer, false);
        model.getEditText().setText(oldVO.getModel());
        fuelEdit.setText(oldVO.getFuelType(), false);
        hybridEdit.setText(oldVO.getHybridType(), false);
        engine.getEditText().setText(String.format(locale, "%f", oldVO.getEngine()));
        transmissionEdit.setText(oldVO.getTransmission(), false);
        modelYear.getEditText().setText(String.format(locale, "%d", oldVO.getModelYear()));
        hp.getEditText().setText(String.format(locale, "%d", oldVO.getHp()));
        torque.getEditText().setText(String.format(locale, "%d", oldVO.getTorque()));

        int max = Math.max(oldVO.getOdoFuelKm(), oldVO.getOdoCostKm());
        max = Math.max(max, oldVO.getOdoRemindKm());
        initKm.getEditText().setText(String.format(locale, "%d", max));
        initKm.getEditText().setEnabled(false);
        model.requestFocus();
        fileName = oldVO.getCustomImg();
        if (fileName != null) {
            logoDelete.setVisibility(View.VISIBLE);
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
        Log.d(TAG, "enterValues: finished");
    }

    /**
     * Display either the custom image or the predefined one
     */
    private void changeImage() {
        Log.d(TAG, "changeImage: started...");
        if (customImage != null) {
            Glide.with(getApplicationContext()).load(customImage).into(logoImg);
        } else {
            try {
                ManufacturerObject manufacturerObject = MainActivity.manufacturers.get(makeEdit.getText().toString());
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
        Log.d(TAG, "changeImage: finished");
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
            fileName = makeEdit.getText().toString() + "_" + Calendar.getInstance().getTimeInMillis()/1000 + ".png";
            Utils.downloadImage(getResources(), getApplicationContext(), customImage, fileName);
        }
    }

    /**
     * Remove custom image
     */
    private void clearCustomImg() {
        Log.d(TAG, "clearCustomImg");
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
            logoDelete.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Save changed vehicle
     */
    private void saveEdit() {
        Log.d(TAG, "saveEdit: started...");

        boolean makeStatus = this.validatorMake.isEmpty();
        boolean modelStatus = this.validatorModel.isEmpty();
        boolean fuelStatus = this.validatorFuelType.isEmpty();
        boolean hybridStatus = this.validatorHybrid.isEmpty();
        boolean engineStatus = this.validatorEngine.isEmpty();
        boolean transmissionStatus = this.validatorTransmission.isEmpty();
        boolean modelYearStatus = this.validatorModelYear.isEmpty();
        boolean powerStatus = this.validatorPower.isEmpty();
        boolean torqueStatus = this.validatorTorque.isEmpty();

        if (makeStatus && modelStatus && fuelStatus && hybridStatus && engineStatus &&
                transmissionStatus && modelYearStatus && powerStatus && torqueStatus) {
            Toast.makeText(this, getString(R.string.fill_text_edits), Toast.LENGTH_LONG).show();
            return;
        }

        VehicleObject vo = new VehicleObject();
        vo.setId(vehicleID);
        vo.setOdoFuelKm(oldVO.getOdoFuelKm());
        vo.setOdoCostKm(oldVO.getOdoCostKm());
        vo.setOdoRemindKm(oldVO.getOdoRemindKm());
        vo.setCustomImg(fileName);

        vo.setHp(hp.getEditText().getText().toString());
        vo.setTorque(Integer.parseInt(torque.getEditText().getText().toString()));
        vo.setFuelType(fuel.getEditText().getText().toString());
        vo.setEngine(Double.parseDouble(engine.getEditText().getText().toString()));
        vo.setTransmission(transmission.getEditText().getText().toString().trim());
        vo.setMake(makeEdit.getText().toString().trim());
        vo.setModel(model.getEditText().getText().toString().trim());
        vo.setHybridType(hybrid.getEditText().getText().toString());
        vo.setModelYear(Integer.parseInt(modelYear.getEditText().getText().toString()));

        dbHelper.updateVehicle(vo);

        if (oldVO.getOdoFuelKm() != vo.getOdoFuelKm()) {
            int diff = vo.getOdoFuelKm() - oldVO.getOdoFuelKm();
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
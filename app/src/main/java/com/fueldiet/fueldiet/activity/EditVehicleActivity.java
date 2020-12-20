package com.fueldiet.fueldiet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EditVehicleActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditVehicleActivity";
    private FuelDietDBHelper dbHelper;
    private long vehicleID;
    private AutoCompleteTextView make, fuel;
    private TextInputLayout model;
    private String fuelSelected;
    private TextInputLayout engine;
    private TextInputLayout hp;
    private TextInputLayout torque;
    private TextInputLayout initKM;
    private TextInputLayout transmission;
    public List<ManufacturerObject> manufacturers;

    private ImageView logoImg;
    private MaterialButton logoSet, logoDelete;
    private Uri customImage;
    private String fileName;

    private VehicleObject oldVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);
        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

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
        logoSet.setOnClickListener(v -> showImagePicker());
        logoDelete.setOnClickListener(v -> clearCustomImg());
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
        torque = findViewById(R.id.add_vehicle_torque_input);
        initKM = findViewById(R.id.add_vehicle_start_km_input);
        transmission = findViewById(R.id.add_vehicle_transmission_input);
        logoImg = findViewById(R.id.add_vehicle_make_logo_img);
        logoSet = findViewById(R.id.add_vehicle_set_img);
        logoDelete = findViewById(R.id.add_vehicle_remove_img);
    }

    /**
     * Fill dropdown lists
     */
    private void fillDropDowns() {
        Log.i("Edit vehicle", "filling dropdown with values");
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
        Collections.sort(manufacturers, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        make.setAdapter(adapter);
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
        make.setText(manufacturer, false);
        model.getEditText().setText(oldVO.getModel());
        hp.getEditText().setText(oldVO.getHp()+"");
        torque.getEditText().setText(oldVO.getTorque()+"");
        engine.getEditText().setText(oldVO.getEngine());
        //if (oldVO.getOdoKm() != 0)
        int max = Math.max(oldVO.getOdoFuelKm(), oldVO.getOdoCostKm());
        max = Math.max(max, oldVO.getOdoRemindKm());
        initKM.getEditText().setText(max + "");
        initKM.getEditText().setEnabled(false);
        transmission.getEditText().setText(oldVO.getTransmission());
        fuelSelected = oldVO.getFuel();
        fuel.setText(fuelSelected, false);
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
            logoDelete.setVisibility(View.VISIBLE);
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
            logoDelete.setVisibility(View.INVISIBLE);
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
        vo.setOdoFuelKm(oldVO.getOdoFuelKm());
        vo.setOdoCostKm(oldVO.getOdoCostKm());
        vo.setOdoRemindKm(oldVO.getOdoRemindKm());

        vo.setCustomImg(fileName);

        ok = ok && vo.setMake(make.getText().toString());
        ok = ok && vo.setModel(model.getEditText().getText().toString());
        ok = ok && vo.setTransmission(transmission.getEditText().getText().toString());
        ok = ok && vo.setEngine(engine.getEditText().getText().toString());
        ok = ok && vo.setFuel(Utils.fromSLOtoENG(fuelSelected));
        ok = ok && vo.setHp(hp.getEditText().getText().toString());
        ok = ok && vo.setTorque(torque.getEditText().getText().toString());

        if (!ok) {
            Toast.makeText(this, getString(R.string.fill_text_edits), Toast.LENGTH_LONG).show();
            return;
        }

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
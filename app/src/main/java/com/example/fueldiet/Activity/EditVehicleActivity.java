package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fueldiet.Adapter.AutoCompleteManufacturerAdapter;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private FloatingActionButton clearImg;
    private Uri customImage;
    private String fileName;

    private List<String> filesToDelete;


    private VehicleObject oldVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_vehicle_new);
        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);
        customImage = null;
        fileName = null;
        filesToDelete = new ArrayList<>();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_vehicle_title);

        linkFields();
        fillDropDowns();
        enterValues();

        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> saveEdit());

        make.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    changeImage();
            }
        });

        logoImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        logoText.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicker();
            }
        });

        clearImg = findViewById(R.id.add_vehicle_clear_custom_img);
        clearImg.setOnClickListener(v -> clearCustomImg());
    }

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

    private void enterValues() {
        Log.i("Edit vehicle", "inserting vehicle values");
        oldVO = dbHelper.getVehicle(vehicleID);
        final String manufacturer = oldVO.getMake();
        make.setText(manufacturer);
        model.getEditText().setText(oldVO.getModel());
        hp.getEditText().setText(oldVO.getHp()+"");
        engine.getEditText().setText(oldVO.getEngine());
        if (oldVO.getInitKM() != 0)
            initKM.getEditText().setText(oldVO.getInitKM()+"");
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
            }
        }
        changeImage();
    }

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
                Glide.with(getApplicationContext()).load(R.drawable.ic_help_outline_black_24dp).into(logoImg);
            } catch (Exception oe) {
                Glide.with(getApplicationContext()).load(R.drawable.ic_help_outline_black_24dp).into(logoImg);
            }
        }
    }

    private void showImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.e("ActivityIntent", "Here");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("ActivityResult", "Here");

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            customImage = data.getData();
            changeImage();
            clearImg.setVisibility(View.VISIBLE);
            fileName = make.getText().toString() + "_" + Calendar.getInstance().getTimeInMillis()/1000 + ".png";
        }
    }

    private void clearCustomImg() {
        filesToDelete.add(fileName);
        customImage = null;
        fileName = null;
        changeImage();
        clearImg.setVisibility(View.INVISIBLE);
    }

    private void saveEdit() {
        Log.i("Edit vehicle", "saving changes");
        VehicleObject vo = new VehicleObject();
        vo.setId(vehicleID);
        boolean ok = true;
        if (initKM.getEditText().getText().toString().equals(""))
            ok = ok && vo.setInitKM(0);
         else
            ok = ok && vo.setInitKM(initKM.getEditText().getText().toString());

        if (customImage != null) {
            vo.setCustomImg(fileName);
            Utils.downloadImage(getResources(), getApplicationContext(), customImage, fileName);
        }

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

        if (oldVO.getInitKM() != vo.getInitKM()) {
            int diff = vo.getInitKM() - oldVO.getInitKM();
            List<DriveObject> oldDrives = dbHelper.getAllDrives(vehicleID);
            for (DriveObject driveObject : oldDrives) {
                int newOdo = driveObject.getOdo() + diff;
                driveObject.setOdo(newOdo);
                dbHelper.updateDriveODO(driveObject);
            }
        }
        try {
            for (String f : filesToDelete) {
                File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
                File img = new File(storageDIR, f);
                img.delete();
            }
        } finally {
            Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
            finish();
        }
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
package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.content.Intent;
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
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileNotFoundException;
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
    private FloatingActionButton clearImg;

    private Uri customImage;
    private String fileName;

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
        logoImg = findViewById(R.id.add_vehicle_make_logo_img);
        logoText = findViewById(R.id.add_vehicle_make_text);
        customImage = null;

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

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.fuel, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuel.setAdapter(adapterS);
        fuel.setOnItemSelectedListener(this);

        dbHelper = new FuelDietDBHelper(this);

        FloatingActionButton addVehicle = findViewById(R.id.add_vehicle_save);
        addVehicle.setOnClickListener(v -> addNewVehicle());
        manufacturers = new ArrayList<>(MainActivity.manufacturers.values());
        AutoCompleteManufacturerAdapter adapter = new AutoCompleteManufacturerAdapter(this, manufacturers);
        make.setAdapter(adapter);
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
        customImage = null;
        changeImage();
        clearImg.setVisibility(View.INVISIBLE);
    }

    private void addNewVehicle() {

        Log.i("BUTTON PRESSED", "Clicked save card_template_vehicle - floating button");

        boolean ok = true;
        VehicleObject vo = new VehicleObject();
        if (initKM.getEditText().getText().toString().equals(""))
            ok = ok && vo.setInitKM(0);
        else
            ok = ok && vo.setInitKM(initKM.getEditText().getText().toString());

        if (customImage != null) {
            vo.setCustomImg(fileName);
            Utils.downloadImage(getResources(), getApplicationContext(), customImage, fileName);
        }

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

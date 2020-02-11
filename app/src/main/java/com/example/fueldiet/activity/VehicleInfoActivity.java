package com.example.fueldiet.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.object.ManufacturerObject;
import com.example.fueldiet.object.VehicleObject;

import java.io.File;

import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleInfoActivity extends BaseActivity {

    private FuelDietDBHelper dbHelper;
    private VehicleObject vehicleObject;
    private TextView make, model;
    private ImageView logo;
    private long vehicle_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new FuelDietDBHelper(this);

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);

        vehicleObject = dbHelper.getVehicle(vehicle_id);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        make = findViewById(R.id.vehicle_info_make);
        model = findViewById(R.id.vehicle_info_model);
        logo = findViewById(R.id.vehicle_info_logo);
        
        make.setText(vehicleObject.getMake());
        model.setText(vehicleObject.getModel());
        
        try {
            String fileName = vehicleObject.getCustomImg();
            File storageDIR = getDir("Images",MODE_PRIVATE);
            if (fileName == null) {
                ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(vehicleObject.getMake()));
                if (!mo.isOriginal()){
                    Utils.downloadImage(getResources(), getApplicationContext(), mo);
                }
                int idResource = getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", getPackageName());
                Glide.with(this).load(storageDIR+"/"+mo.getFileNameMod()).error(getDrawable(idResource)).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(logo);
            } else {
                Glide.with(this).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(logo);
            }
        } catch (Exception e){
            Bitmap noIcon = Utils.getBitmapFromVectorDrawable(this, R.drawable.ic_help_outline_black_24dp);
            Glide.with(this).load(noIcon).fitCenter().diskCacheStrategy(DiskCacheStrategy.ALL).into(logo);
        }
    }
}

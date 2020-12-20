package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.fueldiet.fueldiet.Utils.toCapitalCaseWords;

public class VehicleInfoActivity extends BaseActivity {

    private FuelDietDBHelper dbHelper;
    private VehicleObject vehicleObject;
    private TextView make, model, trueKm, avgCons, unit2;
    private ImageView logo;
    private MaterialButton defaultVehicle;
    private long vehicle_id;

    private final String KMPL = "km/l";
    private String units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = FuelDietDBHelper.getInstance(this);

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", 1);

        vehicleObject = dbHelper.getVehicle(vehicle_id);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        make = findViewById(R.id.vehicle_info_make);
        model = findViewById(R.id.vehicle_info_model);
        trueKm = findViewById(R.id.vehicle_info_true_km);
        avgCons = findViewById(R.id.vehicle_info_avg_cons);
        unit2 = findViewById(R.id.unit2);
        logo = findViewById(R.id.vehicle_info_logo);
        defaultVehicle = findViewById(R.id.vehicle_info_default);

        defaultVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

                    String vehicleName = vehicleObject.getMake() + " " + vehicleObject.getModel();

                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putLong("selected_vehicle", vehicle_id);
                    editor.putString("selected_vehicle_name", vehicleName);
                    editor.apply();

                    pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Log.e("Saved", pref.getString("selected_vehicle_name", "null"));

                    Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mainIntent.setAction(Intent.ACTION_VIEW);

                    Intent vehicleDetails0 = new Intent(getApplicationContext(), VehicleDetailsActivity.class);
                    vehicleDetails0.putExtra("vehicle_id", vehicle_id);
                    vehicleDetails0.putExtra("frag", 0);
                    vehicleDetails0.setAction(Intent.ACTION_VIEW);

                    Intent vehicleDetails1 = (Intent)vehicleDetails0.clone();
                    vehicleDetails1.putExtra("frag", 1);
                    Intent vehicleDetails2 = (Intent)vehicleDetails0.clone();
                    vehicleDetails1.putExtra("frag", 2);

                    Intent addNewFuel = new Intent(getApplicationContext(), AddNewDriveActivity.class);
                    addNewFuel.setAction(Intent.ACTION_VIEW);
                    addNewFuel.putExtra("vehicle_id", vehicle_id);
                    Intent addNewCost = new Intent(getApplicationContext(), AddNewCostActivity.class);
                    addNewCost.setAction(Intent.ACTION_VIEW);
                    addNewCost.putExtra("vehicle_id", vehicle_id);
                    Intent addNewReminder = new Intent(getApplicationContext(), AddNewReminderActivity.class);
                    addNewReminder.setAction(Intent.ACTION_VIEW);
                    addNewReminder.putExtra("vehicle_id", vehicle_id);

                    ShortcutInfo newFuel = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_fuel_add")
                            .setShortLabel(getString(R.string.log_fuel))
                            .setLongLabel(vehicleName)
                            .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_local_gas_station_shortcut_24px))
                            .setIntents(new Intent[]{
                                    mainIntent, vehicleDetails0, addNewFuel
                            })
                            .build();
                    ShortcutInfo newCost = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_cost_add")
                            .setShortLabel(getString(R.string.log_cost))
                            .setLongLabel(vehicleName)
                            .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_euro_symbol_shortcut_24px))
                            .setIntents(new Intent[]{
                                    mainIntent, vehicleDetails1, addNewCost
                            })
                            .build();
                    ShortcutInfo newReminder = new ShortcutInfo.Builder(getApplicationContext(), "shortcut_reminder_add")
                            .setShortLabel(getString(R.string.add_rem))
                            .setLongLabel(vehicleName)
                            .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_notifications_shortcut_24px))
                            .setIntents(new Intent[]{
                                    mainIntent, vehicleDetails2, addNewReminder
                            })
                            .build();

                    if (getSystemService(ShortcutManager.class).getDynamicShortcuts().size() < 2)
                        getSystemService(ShortcutManager.class).addDynamicShortcuts(Arrays.asList(newFuel, newCost, newReminder));
                    else
                        getSystemService(ShortcutManager.class).updateShortcuts(Arrays.asList(newFuel, newCost, newReminder));
                }
            }
        });
        
        make.setText(vehicleObject.getMake());
        model.setText(vehicleObject.getModel());


        /*set true km*/
        List<DriveObject> allDrives = dbHelper.getAllDrives(vehicle_id);
        int allKm = 0;
        double allL = 0.0;

        /*

        for (DriveObject drive : allDrives) {
            allKm += drive.getTrip();
            allL += drive.getLitres();
            if (drive.getFirst() == 1) {
                allL -= drive.getLitres();
                //allKm += drive.getOdo() - drive.getTrip();
                allKm -= drive.getTrip();
            }
        }

        trueKm.setText(allKm+"");
        double cons = 0.0;
        if (allKm != 0 && allL != 0.0) {
            cons = Utils.calculateConsumption(allKm, allL);
        }

        if (PreferenceManager.getDefaultSharedPreferences(this).getString("selected_unit", "litres_per_km").equals("litres_per_km"))
            units = "litres_per_km";
        else
            units = "km_per_litre";

        if (units.equals("km_per_litre")) {
            cons = Utils.convertUnitToKmPL(cons);
            unit2.setText(KMPL);
        } else {
            unit2.setText("l/100km");
        }


        avgCons.setText(String.format("%.2f", cons));

        */
        
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

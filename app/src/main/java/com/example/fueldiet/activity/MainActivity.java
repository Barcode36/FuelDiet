package com.example.fueldiet.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.fragment.CalculatorFragment;
import com.example.fueldiet.fragment.MainFragment;
import com.example.fueldiet.object.ManufacturerObject;
import com.example.fueldiet.object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MainActivity extends BaseActivity {

    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s";

    private long backPressedTime;
    private Toast backToast;
    SharedPreferences pref;
    FuelDietDBHelper dbHelper;
    public static Map<String, ManufacturerObject> manufacturers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new FuelDietDBHelper(this);

        /* Fill Map with Manufacturers Objects from json */
        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = getSharedPreferences("prefs", MODE_PRIVATE);
        long lastVehicleID = pref.getLong("last_vehicle", -1);

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFrag;

            switch (item.getItemId()) {
                case R.id.main_calculator:
                    selectedFrag = CalculatorFragment.newInstance();
                    break;
                /*case R.id.chart_bar:
                    selectedFrag = BarChartFragment.newInstance(vehicle_id, vo);
                    break;*/
                default:
                    //is main
                    selectedFrag = MainFragment.newInstance(lastVehicleID);
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFrag).commit();
            return true;
        });

        bottomNav.setSelectedItemId(R.id.main_home);
    }


    /**
     * Double press back to exit
     */
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), getString(R.string.double_tap_to_exit), Toast.LENGTH_LONG);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    /**
     * Read JSON
     * @return json as String
     */
    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.carlogos);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            if (resultCode == RESULT_OK) {
                String returnedResult = data.getData().toString();
                if (returnedResult.equals("ok")) {

                } else {
                    removeItem(Long.parseLong(returnedResult));
                }
            }
        }
    }

    private void removeItem(final long id) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.vehicle_main_layout), getString(R.string.vehicle_deleted), Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                //show snackbar but only hide element
                super.onShown(sb);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                //if undo was not pressed, delete vehicle, all data and img
                super.onDismissed(transientBottomBar, event);
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    try {
                        VehicleObject vo = dbHelper.getVehicle(id);
                        if (vo.getCustomImg() != null) {
                            File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                            File img = new File(storageDIR, vo.getCustomImg());
                            img.delete();
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity - DeleteCustomImg - " + e.getClass().getSimpleName(), "Custom image was not found");
                    } finally {
                        List<VehicleObject> data = dbHelper.getAllVehiclesExcept(id);
                        boolean exist = false;
                        VehicleObject main = dbHelper.getVehicle(id);
                        //check more than one vehicle of same make
                        if (data == null || data.size() == 0){
                            exist = false;
                        } else {
                            for (VehicleObject vo : data) {
                                if (vo.getMake().equals(main.getMake()) && vo.getId() != main.getId()) {
                                    exist = true;
                                    break;
                                }
                            }
                        }
                        //if not:
                        if (!exist) {
                            try {
                                File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
                                ManufacturerObject mo = MainActivity.manufacturers.get(main.getMake());
                                File img = new File(storageDIR, mo.getFileNameMod());
                                img.delete();
                            } catch (Exception e) {
                                Log.e("MainActivity - DeleteImg - " + e.getClass().getSimpleName(), "Vehicle img was not found, maybe custom make?");
                            }

                        }
                    }
                    dbHelper.deleteVehicle(id);
                    List<Fragment> fragments = getSupportFragmentManager().getFragments();
                    for (Fragment fr : fragments) {
                        if (fr instanceof MainFragment) {
                            ((MainFragment)fr).Update();
                        }
                    }
                }
            }
        }).setAction("UNDO", v -> {
            //reset vehicle
            Toast.makeText(this, getString(R.string.undo_pressed), Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //open setting screen
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.edit_vehicle:
                long selectedID = pref.getLong("last_vehicle", -1);
                if (selectedID == -1)
                    Toast.makeText(this, "Option not possible ATM.", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
                    intent.putExtra("vehicle_id", selectedID);
                    //startActivity(intent);

                    startActivityForResult(intent, 12);
                }
                return true;
            case R.id.reset_db:
                //reset db and prefs
                FuelDietDBHelper dbh = new FuelDietDBHelper(getBaseContext());
                Toast.makeText(this, "Reset is done.", Toast.LENGTH_SHORT).show();
                dbh.resetDb();
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                prefs.edit().clear().apply();
                SharedPreferences pref = getSharedPreferences("prefs", MODE_PRIVATE);
                pref.edit().clear().apply();
                //fillData();
                //mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
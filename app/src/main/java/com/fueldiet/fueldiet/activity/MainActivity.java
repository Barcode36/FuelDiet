package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.CSVWriter;
import com.fueldiet.fueldiet.db.FuelDietContract;
import com.fueldiet.fueldiet.fragment.CalculatorFragment;
import com.fueldiet.fueldiet.fragment.MainFragment;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MainActivity extends BaseActivity {

    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private long backPressedTime;
    private Toast backToast;
    SharedPreferences pref;
    FuelDietDBHelper dbHelper;
    public View fabBgTop;
    public static Map<String, ManufacturerObject> manufacturers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new FuelDietDBHelper(this);
        fabBgTop = findViewById(R.id.main_activity_fab_bg);

        /*SQLiteDatabase db = dbHelper.getWritableDatabase();
        int v = db.getVersion();*/

        /* Fill Map with Manufacturers Objects from json */
        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedVehicle = pref.getLong("selected_vehicle", -1);

        /* dynamic shortcuts */
        final ShortcutManager shortcutManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(ShortcutManager.class);
            Intent mainIntent = new Intent(this, MainActivity.class);
            Intent addNewVehicle = new Intent(this, AddNewVehicleActivity.class);

            mainIntent.setAction(Intent.ACTION_VIEW);
            addNewVehicle.setAction(Intent.ACTION_VIEW);

            ShortcutInfo.Builder shortcutBuilder = new ShortcutInfo.Builder(this, "shortcut_vehicle_add");
            ShortcutInfo newVehicle = shortcutBuilder
                    .setShortLabel(getString(R.string.create_new_vehicle_title))
                    .setLongLabel(getString(R.string.create_new_vehicle_title))
                    .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_add_24px))
                    .setIntents(new Intent[]{
                            mainIntent, addNewVehicle
                    }).build();

            if (selectedVehicle == -1) {
                shortcutManager.setDynamicShortcuts(Collections.singletonList(newVehicle));
            } else {
                Intent vehicleDetails0 = new Intent(this, VehicleDetailsActivity.class);
                vehicleDetails0.putExtra("vehicle_id", selectedVehicle);
                vehicleDetails0.putExtra("frag", 0);
                vehicleDetails0.setAction(Intent.ACTION_VIEW);

                Intent vehicleDetails1 = (Intent)vehicleDetails0.clone();
                vehicleDetails1.putExtra("frag", 1);
                Intent vehicleDetails2 = (Intent)vehicleDetails0.clone();
                vehicleDetails1.putExtra("frag", 2);

                Intent addNewFuel = new Intent(this, AddNewDriveActivity.class);
                addNewFuel.setAction(Intent.ACTION_VIEW);
                addNewFuel.putExtra("vehicle_id", selectedVehicle);
                Intent addNewCost = new Intent(this, AddNewCostActivity.class);
                addNewCost.setAction(Intent.ACTION_VIEW);
                addNewCost.putExtra("vehicle_id", selectedVehicle);
                Intent addNewReminder = new Intent(this, AddNewReminderActivity.class);
                addNewReminder.setAction(Intent.ACTION_VIEW);
                addNewReminder.putExtra("vehicle_id", selectedVehicle);

                VehicleObject vehicle = dbHelper.getVehicle(selectedVehicle);
                String vehicleName = vehicle.getMake() + " " + vehicle.getModel();

                ShortcutInfo newFuel = new ShortcutInfo.Builder(this, "shortcut_fuel_add")
                        .setShortLabel(getString(R.string.log_fuel))
                        .setLongLabel(vehicleName)
                        .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_local_gas_station_shortcut_24px))
                        .setIntents(new Intent[]{
                                mainIntent, vehicleDetails0, addNewFuel
                        })
                        .build();
                ShortcutInfo newCost = new ShortcutInfo.Builder(this, "shortcut_cost_add")
                        .setShortLabel(getString(R.string.log_cost))
                        .setLongLabel(vehicleName)
                        .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_euro_symbol_shortcut_24px))
                        .setIntents(new Intent[]{
                                mainIntent, vehicleDetails1, addNewCost
                        })
                        .build();
                ShortcutInfo newReminder = new ShortcutInfo.Builder(this, "shortcut_reminder_add")
                        .setShortLabel(getString(R.string.add_rem))
                        .setLongLabel(vehicleName)
                        .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_notifications_shortcut_24px))
                        .setIntents(new Intent[]{
                                mainIntent, vehicleDetails2, addNewReminder
                        })
                        .build();

                shortcutManager.setDynamicShortcuts(Arrays.asList(newVehicle, newFuel, newCost, newReminder));

            }
        }

        long lastVehicleID = pref.getLong("last_vehicle", -1);

        BottomNavigationView bottomNav = findViewById(R.id.main_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFrag;

            switch (item.getItemId()) {
                case R.id.main_price_calc:
                case R.id.main_calculator:
                    selectedFrag = CalculatorFragment.newInstance();
                    break;
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        if (pref.getLong("selected_vehicle", -1) == id) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putLong("selected_vehicle", -1);
                            editor.putString("selected_vehicle_name", "No vehicle selected");
                            editor.apply();
                            Toast.makeText(getBaseContext(), "Vehicle shortcut has reset.", Toast.LENGTH_SHORT).show();

                            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                            shortcutManager.removeAllDynamicShortcuts();

                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                            Intent addNewVehicle = new Intent(getApplicationContext(), AddNewVehicleActivity.class);

                            mainIntent.setAction(Intent.ACTION_VIEW);
                            addNewVehicle.setAction(Intent.ACTION_VIEW);

                            ShortcutInfo.Builder shortcutBuilder = new ShortcutInfo.Builder(getBaseContext(), "shortcut_vehicle_add");
                            ShortcutInfo newVehicle = shortcutBuilder
                                    .setShortLabel(getString(R.string.create_new_vehicle_title))
                                    .setLongLabel(getString(R.string.create_new_vehicle_title))
                                    .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_add_24px))
                                    .setIntents(new Intent[]{
                                            mainIntent, addNewVehicle
                                    }).build();

                            shortcutManager.setDynamicShortcuts(Collections.singletonList(newVehicle));
                        }
                    }

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
                    Toast.makeText(this, getString(R.string.not_possible_no_vehicle), Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(MainActivity.this, EditVehicleActivity.class);
                    intent.putExtra("vehicle_id", selectedID);
                    //startActivity(intent);

                    startActivityForResult(intent, 12);
                }
                return true;
            case R.id.add_vehicle:
                startActivity(new Intent(this, AddNewVehicleActivity.class));
                return true;
            /*case R.id.reset_db:
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
                return true;*/
            case R.id.backup_and_restore:
                //dialog to choose either drive or csv
                openBackupDialog();
                //tryToSaveDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openBackupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.backup_and_restore));
        builder.setItems(getResources().getStringArray(R.array.backup_options), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //export to chrome
                } else if (which == 1) {
                    saveDB();
                }
            }
        });
        builder.show();
    }

    private void saveDB() {
        if (hasStoragePermissions()) {
            FuelDietDBHelper dbhelper = new FuelDietDBHelper(this);

            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File file = new File(exportDir, "fueldiet.csv");
            try {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = dbhelper.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + FuelDietContract.VehicleEntry.TABLE_NAME, null);
                csvWrite.writeNext(new String[]{"Vehicles:"});
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    //Which column you want to export
                    String arrStr[] = {
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry._ID)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MODEL)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_KM)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION))
                    };
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.writeNext(new String[]{"Drives:"});
                curCSV = db.rawQuery("SELECT * FROM " + FuelDietContract.DriveEntry.TABLE_NAME, null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    //Which column you want to export
                    String arrStr[] = {
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry._ID)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_ODO_KM)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CAR)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_FIRST)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOT_FULL)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOTE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PETROL_STATION))
                    };
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.writeNext(new String[]{"Costs:"});
                curCSV = db.rawQuery("SELECT * FROM " + FuelDietContract.CostsEntry.TABLE_NAME, null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    //Which column you want to export
                    String arrStr[] = {
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry._ID)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_ODO)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_EXPENSE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_CAR)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DETAILS)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TITLE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TYPE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_RESET_KM))
                    };
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.writeNext(new String[]{"Reminders:"});
                curCSV = db.rawQuery("SELECT * FROM " + FuelDietContract.ReminderEntry.TABLE_NAME, null);
                csvWrite.writeNext(curCSV.getColumnNames());
                while (curCSV.moveToNext()) {
                    //Which column you want to export
                    String arrStr[] = {
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry._ID)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DATE)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_CAR)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DETAILS)),
                            curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_TITLE))
                    };
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                Snackbar.make(getCurrentFocus(), getString(R.string.export_done), Snackbar.LENGTH_SHORT).show();
            } catch (Exception sqlEx) {
                Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
            }
        }
    }

    public void requestStoragePermission() {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Storage permissions granted, save CSV
                saveDB();
            } else {
                Snackbar.make(getCurrentFocus(), getString(R.string.export_issue_permissions), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.i("FuelDiet", "Storage permissions granted.");

        if (permission == PackageManager.PERMISSION_DENIED) {
            requestStoragePermission();
            return false;
        } else {
            return true;
        }
    }
}
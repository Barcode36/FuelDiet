package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.CalculatorFragment;
import com.fueldiet.fueldiet.fragment.MainFragment;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    public static final String LOGO_URL = "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/images/%s";

    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int BACKUP_AND_RESTORE = 2;
    public static final int RESULT_BACKUP = 19;
    public static final int RESULT_RESTORE = 20;

    private ConstraintLayout loadingScreen;
    private ProgressBar loadingBar;
    private TextView loadingMessage;

    private long backPressedTime;
    private Toast backToast;
    SharedPreferences pref;
    FuelDietDBHelper dbHelper;
    public View fabBgTop;
    BottomNavigationView bottomNav;
    public static Map<String, ManufacturerObject> manufacturers;

    private static final int REMOVE_ITEM = 12;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new FuelDietDBHelper(this);
        fabBgTop = findViewById(R.id.main_activity_fab_bg);

        /* Fill Map with Manufacturers Objects from json */
        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingScreen = findViewById(R.id.loading_screen);
        loadingBar = findViewById(R.id.loading_bar);
        loadingMessage = findViewById(R.id.loading_message);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String selectedVehicleString = pref.getString("selected_vehicle", null);
        Long selectedVehicle = Long.getLong(selectedVehicleString);

        if (pref.getString("default_km_mode", "none").equals("none")) {
            pref.edit().putString("default_km_mode", getString(R.string.total_meter)).apply();
        }

        /* dynamic shortcuts */
        //moved to SettingsActivity

        long lastVehicleID = pref.getLong("last_vehicle", -1);

        bottomNav = findViewById(R.id.main_bottom_nav);
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
        if (requestCode == REMOVE_ITEM) {
            if (resultCode == RESULT_OK) {
                String returnedResult = data.getData().toString();
                if (!returnedResult.equals("ok")) {
                    removeItem(Long.parseLong(returnedResult));
                }
            }
        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, PERMISSIONS_STORAGE))
                startActivity(new Intent(this, BackupAndRestore.class));
        } else if (requestCode == BACKUP_AND_RESTORE) {
            if (resultCode == RESULT_BACKUP) {
                //create backup
                BackupRestoreRunnable runnable = new BackupRestoreRunnable(RESULT_BACKUP, data.getData());
                new Thread(runnable).start();
            } else if (resultCode == RESULT_RESTORE){
                //override data
                BackupRestoreRunnable runnable = new BackupRestoreRunnable(RESULT_RESTORE, data.getData());
                new Thread(runnable).start();
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
                        Log.e(TAG, "onDismissed: Custom image was not found", e.fillInStackTrace());
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
                                Log.e(TAG, "onDismissed: Vehicle img was not found, maybe custom make?", e.fillInStackTrace());
                            }

                        }
                    }
                    dbHelper.deleteVehicle(id);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        String selected = pref.getString("selected_vehicle", null);
                        if (selected != null && Long.parseLong(selected) == id) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.remove("selected_vehicle").apply();
                            Toast.makeText(getBaseContext(), "Vehicle shortcut has reset.", Toast.LENGTH_SHORT).show();

                            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                            assert shortcutManager != null;
                            shortcutManager.removeAllDynamicShortcuts();
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
                //restore and backup
                checkStoragePermissions();
                return true;
            case R.id.petrol_stations_edit:
                startActivity(new Intent(MainActivity.this, PetrolStationsOverview.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @AfterPermissionGranted(REQUEST_EXTERNAL_STORAGE)
    private void checkStoragePermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS_STORAGE))
            startActivityForResult(new Intent(this, BackupAndRestore.class), BACKUP_AND_RESTORE);
            //startActivity(new Intent(this, BackupAndRestore.class));
        else
            EasyPermissions.requestPermissions(this, "Storage permission is required for backup to work",
                    REQUEST_EXTERNAL_STORAGE, PERMISSIONS_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startActivity(new Intent(this, BackupAndRestore.class));
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: "+ requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    class BackupRestoreRunnable implements Runnable {
        private static final String TAG = "BackupRestoreRunnable";

        int command;
        Uri path;
        private String msg;

        BackupRestoreRunnable(int command, Uri path) {
            this.command = command;
            this.path = path;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    if (command == RESULT_BACKUP)
                        loadingMessage.setText("Creating a backup");
                    else
                        loadingMessage.setText("Restoring data");
                }
            });
            Fragment tmp = getSupportFragmentManager().getFragments().get(0);
            getSupportFragmentManager().beginTransaction().remove(tmp).commit();
            if (command == RESULT_BACKUP) {
                msg = Utils.createCSVfile(path, getApplicationContext());
            } else if (command == RESULT_RESTORE) {
                msg = Utils.readCSVfile(path, getApplicationContext());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingMessage.setText(msg);
                }
            });
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //reopen fragment
            if (tmp instanceof MainFragment)
                bottomNav.setSelectedItemId(R.id.main_home);
            else if (tmp instanceof CalculatorFragment)
                bottomNav.setSelectedItemId(R.id.main_calculator);
            else
                bottomNav.setSelectedItemId(R.id.main_price_calc);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingScreen.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingMessage.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
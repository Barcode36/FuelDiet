package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.dialog.LoadingDialog;
import com.fueldiet.fueldiet.fragment.CalculatorFragment;
import com.fueldiet.fueldiet.fragment.FuelPricesMainFragment;
import com.fueldiet.fueldiet.fragment.MainFragment;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener {
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
    public static final int SETTINGS_ACTION = 3;

    private FrameLayout fragmentScreen;
    SharedPreferences pref;
    FuelDietDBHelper dbHelper;
    public View fabBgTop;
    DrawerLayout drawerLayout;
    public static Map<String, ManufacturerObject> manufacturers;

    private static final int REMOVE_ITEM = 12;

    private Fragment selectedFrag;
    private long lastVehicleID;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = new LoadingDialog(this);
        dbHelper = FuelDietDBHelper.getInstance(this);
        fabBgTop = findViewById(R.id.main_activity_fab_bg);

        Intent intent = getIntent();
        Log.d(TAG, "onCreate: displayPrice " + intent.getBooleanExtra("displayPrice", false));

        /* Fill Map with Manufacturers Objects from json */
        String response = loadJSONFromAsset();
        List<ManufacturerObject> tmp = new Gson().fromJson(response, new TypeToken<List<ManufacturerObject>>() {}.getType());
        manufacturers = tmp.stream().collect(Collectors.toMap(ManufacturerObject::getName, manufacturerObject -> manufacturerObject));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentScreen = findViewById(R.id.main_fragment_container);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (pref.getString("default_km_mode", "none").equals("none")) {
            pref.edit().putString("default_km_mode", getString(R.string.total_meter)).apply();
        }

        /* dynamic shortcuts */
        //some moved to SettingsActivity and Utils

        lastVehicleID = pref.getLong("last_vehicle", -1);


        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("displayPrice", false)) {
                selectedFrag = FuelPricesMainFragment.newInstance();
            } else {
                selectedFrag = MainFragment.newInstance(lastVehicleID);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                    selectedFrag).commit();
            if (getIntent().getBooleanExtra("displayPrice", false)) {
                navigationView.setCheckedItem(R.id.main_stations_price);
            } else {
                navigationView.setCheckedItem(R.id.main_home);
            }
        }

        /* create petrol station logos from db */
        PetrolStationRunnable runnable = new PetrolStationRunnable(dbHelper.getAllPetrolStations());
        new Thread(runnable).start();

    }

    private void loadingDialogVisibility(boolean shown) {
        if (shown) {
            if (!loadingDialog.isDisplayed()) {
                loadingDialog.showDialog();
            }
        } else {
            if (loadingDialog.isDisplayed()) {
                loadingDialog.hideDialog();
            }
        }
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

            if (EasyPermissions.hasPermissions(this, PERMISSIONS_STORAGE)) {
                startActivity(new Intent(this, BackupAndRestore.class));
            }

        } else if (requestCode == BACKUP_AND_RESTORE) {
            if (resultCode == RESULT_BACKUP) {
                //create backup
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                    BackupRestoreRunnable runnable = new BackupRestoreRunnable(RESULT_BACKUP, outputStream);
                    new Thread(runnable).start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == RESULT_RESTORE){
                //override data
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    BackupRestoreRunnable runnable = new BackupRestoreRunnable(RESULT_RESTORE, inputStream);
                    new Thread(runnable).start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == SETTINGS_ACTION) {
            finish();
            startActivity(getIntent());
        }
    }

    private void removeItem(final long id) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), getString(R.string.vehicle_deleted), Snackbar.LENGTH_LONG);
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
        InputStream inputStream;
        Uri uri;
        OutputStream outputStream;
        private String msg;

        BackupRestoreRunnable(int command, InputStream inputStream) {
            this.command = command;
            this.inputStream = inputStream;
        }

        BackupRestoreRunnable(int command, Uri uri) {
            this.command = command;
            this.uri = uri;
        }

        BackupRestoreRunnable(int command, OutputStream outputStream) {
            this.command = command;
            this.outputStream = outputStream;
        }


        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragmentScreen.setVisibility(View.INVISIBLE);
                    /*bottomNav.setSelected(false);/*
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    if (command == RESULT_BACKUP)
                        loadingMessage.setText("Creating a backup");
                    else
                        loadingMessage.setText("Restoring data");*/
                    loadingDialogVisibility(true);
                }
            });
            getSupportFragmentManager().beginTransaction().detach(selectedFrag).commit();

            if (command == RESULT_BACKUP) {
                msg = Utils.createCSVfile(outputStream, getApplicationContext());
            } else if (command == RESULT_RESTORE) {
                msg = Utils.readCSVfile(inputStream, getApplicationContext());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getSupportFragmentManager().beginTransaction().attach(selectedFrag).commit();
                    fragmentScreen.setVisibility(View.INVISIBLE);
                    /*bottomNav.setSelected(false);/*
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    loadingMessage.setText(msg);*/
                }
            });

            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectedFrag instanceof MainFragment)
                        ((MainFragment) selectedFrag).Update();
                    /*bottomNav.setSelected(true);/*
                    loadingScreen.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingMessage.setVisibility(View.INVISIBLE);
                    fragmentScreen.setVisibility(View.VISIBLE);*/
                    loadingDialogVisibility(false);
                }
            });
        }
    }

    class PetrolStationRunnable implements Runnable {
        List<PetrolStationObject> stationObjects;
        private static final String TAG = "PetrolStationRunnable";

        public PetrolStationRunnable(List<PetrolStationObject> stations) {
            stationObjects = stations;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragmentScreen.setVisibility(View.INVISIBLE);
                    /*bottomNav.setSelected(false);
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    loadingMessage.setText("Preparing images");*/
                    loadingDialogVisibility(true);
                }
            });
            //check for each if logo exists, if not extract it.
            for (PetrolStationObject station : stationObjects) {
                Log.d(TAG, "run: ".concat(station.getName()));
                File storageDIR = getDir("Images",MODE_PRIVATE);
                File imageFile = new File(storageDIR, station.getFileName());
                if (!imageFile.exists()) {
                    //image does not exists yet
                    Log.d(TAG, "run: image is not yet extracted from db");
                    Utils.downloadPSImage(getApplicationContext(), station);
                }
                //maybe delete it from db?
                if (station.getOrigin() == 0)
                    dbHelper.updatePetrolStation(station);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*bottomNav.setSelected(true);
                    loadingScreen.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingMessage.setVisibility(View.INVISIBLE);*/
                    loadingDialogVisibility(false);
                    fragmentScreen.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_home:
                selectedFrag = MainFragment.newInstance(lastVehicleID);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                        selectedFrag).commit();
                break;
            case R.id.main_calculator:
            case R.id.main_price_calc:
                selectedFrag = CalculatorFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                        selectedFrag).commit();
                break;
            case R.id.main_stations_price:
                selectedFrag = FuelPricesMainFragment.newInstance();
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,
                        selectedFrag).commit();
                break;
            case R.id.backup_and_restore:
                //in android 10+ automatic backups are saved to app specific storage, so permission is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivityForResult(new Intent(this, BackupAndRestore.class), BACKUP_AND_RESTORE);
                } else {
                    checkStoragePermissions();
                }
                break;
            case R.id.petrol_stations_edit:
                startActivity(new Intent(MainActivity.this, PetrolStationsOverview.class));
                break;
            case R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_ACTION);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
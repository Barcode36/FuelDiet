package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
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
import com.fueldiet.fueldiet.fragment.FuelPricesMainFragment;
import com.fueldiet.fueldiet.fragment.MainFragment;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
    private FrameLayout fragmentScreen;
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

    private Fragment selectedFrag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = FuelDietDBHelper.getInstance(this);
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
        fragmentScreen = findViewById(R.id.main_fragment_container);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (pref.getString("default_km_mode", "none").equals("none")) {
            pref.edit().putString("default_km_mode", getString(R.string.total_meter)).apply();
        }

        /* dynamic shortcuts */
        //moved to SettingsActivity

        long lastVehicleID = pref.getLong("last_vehicle", -1);

        bottomNav = findViewById(R.id.main_bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            selectedFrag = null;

            switch (item.getItemId()) {
                case R.id.main_price_calc:
                    //selectedFrag = ConverterFragment.newInstance();
                    //break;
                case R.id.main_calculator:
                    selectedFrag = CalculatorFragment.newInstance();
                    break;
                case R.id.main_stations_price:
                    selectedFrag = FuelPricesMainFragment.newInstance();
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

        if (pref.getString("country_select", "other").equals("other")) {
            MenuItem item = bottomNav.getMenu().findItem(R.id.main_stations_price);
            item.setVisible(false);
            bottomNav.setSelectedItemId(R.id.main_home);
        }

        /* create petrol station logos from db */
        PetrolStationRunnable runnable = new PetrolStationRunnable(dbHelper.getAllPetrolStations());
        new Thread(runnable).start();

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
                //in android 10+ automatic backups are saved to app specific storage, so permission is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startActivityForResult(new Intent(this, BackupAndRestore.class), BACKUP_AND_RESTORE);
                } else {
                    checkStoragePermissions();
                }
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
                    bottomNav.setSelected(false);
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    if (command == RESULT_BACKUP)
                        loadingMessage.setText("Creating a backup");
                    else
                        loadingMessage.setText("Restoring data");
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
                    bottomNav.setSelected(false);
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectedFrag instanceof MainFragment)
                        ((MainFragment) selectedFrag).Update();
                    bottomNav.setSelected(true);
                    loadingScreen.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingMessage.setVisibility(View.INVISIBLE);
                    fragmentScreen.setVisibility(View.VISIBLE);
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
                    bottomNav.setSelected(false);
                    loadingScreen.setVisibility(View.VISIBLE);
                    loadingBar.setVisibility(View.VISIBLE);
                    loadingMessage.setVisibility(View.VISIBLE);
                    loadingMessage.setText("Preparing images");
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
                    bottomNav.setSelected(true);
                    loadingScreen.setVisibility(View.GONE);
                    loadingBar.setVisibility(View.INVISIBLE);
                    loadingMessage.setVisibility(View.INVISIBLE);
                    fragmentScreen.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.adapter.SpinnerPetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddNewDriveActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_FINE_LOCATION = 2;
    private static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;

    private long vehicleID;
    private FuelDietDBHelper dbHelper;
    private Context context;
    private Activity activity;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private Spinner selectKM;
    private TextInputLayout inputKM;
    private TextView prevKM;

    private TextInputLayout inputL;
    private TextInputLayout inputLPrice;
    private TextInputLayout inputPricePaid;
    private TextInputLayout inputNote;
    private TextInputLayout inputLatitude;
    private TextInputLayout inputLongitude;
    private Spinner selectPetrolStation;
    private SearchableSpinner selectCountry;

    private Switch firstFuel;
    private Switch notFull;
    private int firstFuelStatus;
    private int notFullStatus;

    private List<String> codes;
    private List<String> names;

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;
    String kmMode;

    TextWatcher fullprice;
    TextWatcher litreprice;
    TextWatcher litres;

    private VehicleObject vo;
    private Calendar hidCalendar;
    Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);
        context = this;
        activity = this;

        vo = dbHelper.getVehicle(vehicleID);
        lastLocation = null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        kmMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("default_km_mode", getString(R.string.total_meter));

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        hidCalendar = Calendar.getInstance();

        initVariables();
        fillVariables();

        /* fill dropdown list */
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.km_types, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectKM.setAdapter(adapterS);
        if (kmMode.equals(getString(R.string.total_meter)))
            selectKM.setSelection(0);
        else
            selectKM.setSelection(1);
        selectKM.setOnItemSelectedListener(this);

        /* open time dialog */
        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.setArguments(currentDate);
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        /* open date dialog */
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

        firstFuel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    firstFuelStatus = 1;
                else
                    firstFuelStatus = 0;
            }
        });

        notFull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    notFullStatus = 1;
                else
                    notFullStatus = 0;
            }
        });

        /* updates fuel, price, full price fields */
        fullprice = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                removeTextListener(0);
                if (inputL.getEditText().getText().toString().equals("")) {
                    addTextListener(0);
                    return;
                } if (s.toString().equals("")) {
                    inputLPrice.getEditText().setText("");
                    addTextListener(0);
                    return;
                }
                double total = Double.parseDouble(s.toString());
                double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                inputLPrice.getEditText().setText(String.valueOf(Utils.calculateLitrePrice(total, litres)));
                addTextListener(0);
            }
        };

        litreprice = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                removeTextListener(1);
                if (inputL.getEditText().getText().toString().equals("")) {
                    addTextListener(1);
                    return;
                } if (s.toString().equals("")) {
                    inputPricePaid.getEditText().setText("");
                    addTextListener(1);
                    return;
                }
                try {
                    double lprice = Double.parseDouble(s.toString());
                    double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                    inputPricePaid.getEditText().setText(String.valueOf(Utils.calculateFullPrice(lprice, litres)));
                } catch (Exception e) {
                    Log.e("AddNewDriveActivity", e.getMessage());
                }
                addTextListener(1);
            }
        };

        litres = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) {
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    inputPricePaid.getEditText().setText("");
                    return;
                }
                if (!inputLPrice.getEditText().getText().toString().equals("")) {
                    removeTextListener(1);
                    try {
                        double lprice = Double.parseDouble(inputLPrice.getEditText().getText().toString());
                        double litres = Double.parseDouble(s.toString());
                        inputPricePaid.getEditText().setText(String.valueOf(Utils.calculateFullPrice(lprice, litres)));
                    } catch (Exception e) {
                        Log.e("AddNewDriveActivity", e.getMessage());
                    }
                    addTextListener(1);
                } else if (!inputPricePaid.getEditText().getText().toString().equals("")) {
                    removeTextListener(0);
                    try {
                        double total = Double.parseDouble(s.toString());
                        double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                        inputLPrice.getEditText().setText(String.valueOf(Utils.calculateLitrePrice(total, litres)));
                    }  catch (Exception e) {
                        Log.e("AddNewDriveActivity", e.getMessage());
                    }
                    addTextListener(0);
                }
            }
        };

        inputPricePaid.getEditText().addTextChangedListener(fullprice);
        inputLPrice.getEditText().addTextChangedListener(litreprice);
        inputL.getEditText().addTextChangedListener(litres);

        /*
        gps
         */
        checkGPSPermissions();

        /* save drive */
        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> addNewDrive());
    }

    /**
     * Removes textlistener when programmatically updating text
     * @param where boolean which to remove
     */
    private void removeTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().removeTextChangedListener(litreprice);
        } else {
            inputPricePaid.getEditText().removeTextChangedListener(fullprice);
        }
    }

    /**
     * Add textlistener when programmatically updating text finished
     * @param where boolean which to add back
     */
    private void addTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().addTextChangedListener(litreprice);
        } else {
            inputPricePaid.getEditText().addTextChangedListener(fullprice);
        }
    }

    /**
     * Create connection between fields and variables
     */
    private void initVariables() {
        inputDate = findViewById(R.id.add_drive_date_input);
        inputTime = findViewById(R.id.add_drive_time_input);

        inputKM = findViewById(R.id.add_drive_km_input);
        selectKM = findViewById(R.id.add_drive_km_mode_spinner);
        prevKM = findViewById(R.id.add_drive_prev_km);

        inputL = findViewById(R.id.add_drive_litres_input);
        inputLPrice = findViewById(R.id.add_drive_price_per_l_input);
        inputPricePaid = findViewById(R.id.add_drive_total_cost_input);
        inputNote = findViewById(R.id.add_drive_note_input);
        selectPetrolStation = findViewById(R.id.add_drive_petrol_station_spinner);
        selectCountry = findViewById(R.id.add_drive_country_spinner);

        firstFuel = findViewById(R.id.add_drive_first_fuelling);
        notFull = findViewById(R.id.add_drive_not_full);
        inputLatitude = findViewById(R.id.add_drive_latitude_input);
        inputLongitude = findViewById(R.id.add_drive_longitude_input);
    }

    /**
     * Set current date and time
     */
    private void fillVariables() {
        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(hidCalendar.getTime()));

        SpinnerPetrolStationAdapter adapter = new SpinnerPetrolStationAdapter(this, getResources().getStringArray(R.array.petrol_stations));
        selectPetrolStation.setAdapter(adapter);
        selectPetrolStation.setSelection(7);

        Locale locale;
        String lang = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("language_select", "english");
        if ("slovene".equals(lang)) {
            locale = new Locale("sl", "SI");
        } else {
            locale = new Locale("en", "GB");
        }

        String[] countryCodes = Locale.getISOCountries();
        codes = new ArrayList<>();
        names = new ArrayList<>();
        for (String countryCode : countryCodes) {
            Locale obj = new Locale("", countryCode);
            codes.add(obj.getCountry());
            names.add(obj.getDisplayCountry(locale));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCountry.setAdapter(spinnerArrayAdapter);
        selectCountry.setTitle(getString(R.string.select_lang).split(" ")[0] + " " + getString(R.string.country).toLowerCase());

        //selectCountry.setSelection(spinnerArrayAdapter.getPosition("SI"));
        selectCountry.setSelection(codes.indexOf("SI"));

        if (dbHelper.getAllDrives(vehicleID) == null || dbHelper.getAllDrives(vehicleID).size() == 0) {
            firstFuel.setChecked(true);
            firstFuel.setEnabled(false);
            firstFuelStatus = 1;
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String note = pref.getString("saved_note", "");

        if (!note.equals("")) {
            inputNote.getEditText().setText(note);
        }

        displayKMmode();
    }

    /**
     * Saves new drive
     */
    private void addNewDrive() {
        final DriveObject driveObject = new DriveObject();
        boolean ok = true;

        ok = ok && driveObject.setCarID(vehicleID);
        ok = ok && driveObject.setCostPerLitre(inputLPrice.getEditText().getText().toString());
        ok = ok && driveObject.setLitres(inputL.getEditText().getText().toString());

        ok = ok && driveObject.setDate(hidCalendar);
        String displayStringKm = inputKM.getEditText().getText().toString();

        /* checks if everything is correct */
        if (!ok || displayStringKm.equals("")) {
            Toast.makeText(this, getString(R.string.fill_text_cost), Toast.LENGTH_LONG).show();
            return;
        }

        driveObject.setFirst(firstFuelStatus);
        driveObject.setNotFull(notFullStatus);

        final int displayKm = Integer.parseInt(displayStringKm);
        DriveObject prevDrive = dbHelper.getPrevDrive(vehicleID);
        String stringNote = inputNote.getEditText().getText().toString();
        if (stringNote == null || stringNote.length() == 0)
            stringNote = null;
        driveObject.setNote(stringNote);

        String station = Utils.fromSLOtoENG(selectPetrolStation.getSelectedItem().toString());
        driveObject.setPetrolStation(station);
        driveObject.setCountry(codes.get(names.indexOf(selectCountry.getSelectedItem().toString())));
        if (lastLocation != null) {
            driveObject.setLatitude(lastLocation.getLatitude());
            driveObject.setLongitude(lastLocation.getLongitude());
        }

        if (kmMode.equals(getString(R.string.total_km))) {
            //vo.setOdoKm(vo.getOdoKm() + displayKm);
            //if (prevDrive != null && prevDrive.getOdo() > displayKm) {
            if (vo.getOdoFuelKm() > displayKm) {
                Toast.makeText(this, getString(R.string.km_is_smaller_than_prev), Toast.LENGTH_SHORT).show();
                return;
            }
            if (prevDrive == null) {
                //the first
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - vo.getOdoFuelKm());
                vo.setOdoFuelKm(displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else if (hidCalendar.getTimeInMillis() < driveObject.getDateEpoch()*1000) {
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            } else {
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - vo.getOdoFuelKm());
                vo.setOdoFuelKm(displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            }
        } else {
            //vo.setOdoKm(vo.getOdoKm() + displayKm);
            if (prevDrive == null) {
                //the first
                driveObject.setOdo(vo.getOdoFuelKm() + displayKm);
                driveObject.setTrip(displayKm);
                vo.setOdoFuelKm(vo.getOdoFuelKm() + displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else if (hidCalendar.getTimeInMillis() < prevDrive.getDateEpoch()*1000) {
                //Toast.makeText(this, getString(R.string.time_is_before_prev), Toast.LENGTH_SHORT).show();
                //return;
                DriveObject biggest = dbHelper.getLastDrive(vehicleID);
                List<DriveObject> newer = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, hidCalendar.getTimeInMillis() / 1000 +10,biggest.getDateEpoch()+10);
                int sumTrip = 0;
                for (DriveObject drive : newer) {
                    int newOdo = drive.getOdo() + displayKm;
                    sumTrip += drive.getTrip();
                    drive.setOdo(newOdo);
                    dbHelper.updateDriveODO(drive);
                }
                //prevDrive = dbHelper.getPrevDriveSelection(vehicleID, prevDrive.getOdo());
                //driveObject.setOdo(prevDrive.getOdo() + displayKm);
                driveObject.setOdo(vo.getOdoFuelKm() - sumTrip + displayKm);
                driveObject.setTrip(displayKm);
                vo.setOdoFuelKm(vo.getOdoFuelKm() + displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else {
                driveObject.setOdo(vo.getOdoFuelKm() + displayKm);
                driveObject.setTrip(displayKm);
                vo.setOdoFuelKm(vo.getOdoFuelKm() + displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            }
        }

        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        prefEditor.remove("saved_note");
        prefEditor.apply();

        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
    }

    /**
     * Changes and updates km mode
     * @param position selected km mode
     */
    private void changeKMmode(int position) {
        switch (position) {
            case 0:
                kmMode = getString(R.string.total_meter);
                break;
            case 1:
                kmMode = getString(R.string.trip_meter);
                break;
        }
        displayKMmode();
        displayPrevKM();
    }

    /**
     * Display chosen km mode
     */
    private void displayKMmode() {
        if (kmMode.equals(getString(R.string.total_meter)))
            inputKM.setHint(getString(R.string.total_meter));
        else
            inputKM.setHint(getString(R.string.trip_meter).substring(0,1).toUpperCase() + getString(R.string.trip_meter).substring(1));
    }

    /**
     * Display previous drive odo
     */
    private void displayPrevKM() {
        //DriveObject driveObject = dbHelper.getPrevDrive(vehicleID);
        //if (driveObject == null)
            prevKM.setText(String.format("odo: %dkm", vo.getOdoFuelKm()));
        //else
        //    prevKM.setText(String.format("odo: %dkm", driveObject.getOdo()));
    }

    /**
     * Updates selected km mode
     * @param parent parent
     * @param view view
     * @param position selected km mode
     * @param id id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        changeKMmode(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        kmMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("default_km_mode", getString(R.string.total_meter));
    }

    /**
     * Updates calendar with new time
     * @param view view
     * @param hourOfDay selected hour
     * @param minute selected minutes
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hidCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        hidCalendar.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
    }

    /**
     * Updates calendar with new date
     * @param view view
     * @param year selected
     * @param month selected month
     * @param dayOfMonth selected day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        hidCalendar.set(Calendar.YEAR, year);
        hidCalendar.set(Calendar.MONTH, month);
        hidCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(hidCalendar.getTime());
        inputDate.getEditText().setText(date);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("km", (inputKM.getEditText().getText().toString()));
        outState.putString("litre", (inputL.getEditText().getText().toString()));
        outState.putString("price_litre", (inputLPrice.getEditText().getText().toString()));
        outState.putString("price", (inputPricePaid.getEditText().getText().toString()));
        outState.putString("note", inputNote.getEditText().getText().toString());

        outState.putString("date", sdfDate.format(hidCalendar.getTime()));
        outState.putString("time", sdfTime.format(hidCalendar.getTime()));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        inputKM.getEditText().setText(savedInstanceState.getString("km"));
        inputL.getEditText().setText(savedInstanceState.getString("litre"));
        inputLPrice.getEditText().setText(savedInstanceState.getString("price_litre"));
        inputPricePaid.getEditText().setText(savedInstanceState.getString("price"));
        inputNote.getEditText().setText(savedInstanceState.getString("note"));

        String date = savedInstanceState.getString("date");
        String time = savedInstanceState.getString("time");

        inputDate.getEditText().setText(date);
        inputTime.getEditText().setText(time);

        String[] dateS = date.split("\\.");
        String[] timeS = time.split("\\:");
        hidCalendar.set(Integer.parseInt(dateS[2]), Integer.parseInt(dateS[1]),
                Integer.parseInt(dateS[0]), Integer.parseInt(timeS[0]),
                Integer.parseInt(timeS[1]));
    }

    private void checkGPSPermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS_LOCATION))
            //start async get location
            getLocationService();
        else
            EasyPermissions.requestPermissions(this, "Location permission is required for getting the location of the petrol station. If permission is not granted, fuel log will be created without petrol station's location.",
                    REQUEST_FINE_LOCATION, PERMISSIONS_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        getLocationService();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d("AddDriveActivity", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        inputLatitude.setHint(getString(R.string.disabled_gps));
        inputLongitude.setHint(getString(R.string.disabled_gps));
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            if (EasyPermissions.hasPermissions(this, PERMISSIONS_LOCATION))
                getLocationService();
            else {
                inputLatitude.setHint(getString(R.string.disabled_gps));
                inputLongitude.setHint(getString(R.string.disabled_gps));
            }
        } else if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.i("TAG", "onActivityResult: GPS Enabled by user");
                    getOneLocationUpdate();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.i("TAG", "onActivityResult: User rejected GPS request");
                    inputLatitude.setHint(getString(R.string.disabled_gps));
                    inputLongitude.setHint(getString(R.string.disabled_gps));
                    break;
                default:
                    break;
            }
        }
    }

    private void getLocationService() {
        client = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10* 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        /* Prompt to turn on gps */

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Log.i("TAG", "onSuccess: location is already enabled");
                getOneLocationUpdate();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.i("TAG", "onFailure: location is not (yet) enabled");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(AddNewDriveActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        inputLatitude.setHint(getString(R.string.latitude));
                        inputLongitude.setHint(getString(R.string.longitude));
                        inputLatitude.getEditText().setText(location.getLatitude() + " ");
                        inputLongitude.getEditText().setText(location.getLongitude() + " ");
                        lastLocation = location;
                        if (client != null) {
                            client.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };
    }

    private void getOneLocationUpdate() {
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}

package com.fueldiet.fueldiet.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.adapter.SpinnerPetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.TimeDatePickerHelper;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AddNewDriveActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "AddNewDriveActivity";

    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int REQUEST_LOCATION = 1324;
    private static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;

    private long vehicleId;
    private FuelDietDBHelper dbHelper;

    private Spinner selectKm;
    private TextInputLayout inputDate, inputTime, inputKm, inputL, inputLPrice, inputPricePaid, inputNote, inputLatitude, inputLongitude;
    private TextInputEditText inputDateEdit, inputTimeEdit, inputKmEdit, inputLEdit, inputLPriceEdit, inputPricePaidEdit, inputNoteEdit, inputLatitudeEdit, inputLongitudeEdit;
    private AutoCompleteTextView selectPetrolStationSpinner;
    private SearchableSpinner selectCountry;
    private Button setLocation;
    private ImageView petrolStationLogo;

    private SwitchMaterial firstFuel, notFull;
    private int firstFuelStatus;
    private int notFullStatus;

    private List<String> codes;
    private List<String> names;

    SimpleDateFormat sdfDate, sdfTime;
    String kmMode;

    TextWatcher fullPrice, litrePrice, litres;

    private VehicleObject vo;
    private Calendar hidCalendar;
    Timer timer;
    private Locale locale;
    private LatLng locationCoords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);
        Log.d(TAG, "onCreate: started");

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleId = intent.getLongExtra("vehicle_id", (long) 1);
        dbHelper = new FuelDietDBHelper(this);
        locationCoords = null;

        vo = dbHelper.getVehicle(vehicleId);
        lastLocation = null;

        kmMode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("default_km_mode", getString(R.string.total_meter));

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        hidCalendar = Calendar.getInstance();

        initVariables();
        fillVariables();
        //gps
        checkGPSPermissions();
        addListenersForPriceCalculation();
        overrideTimeAndDateInputs();
        setOnClickListeners();
        addValidators();


        Log.d(TAG, "onCreate: finished");
    }

    private void startMap() {
        Log.d(TAG, "startMap: opening map to select location");
        Intent mapIntent = new Intent(this, MapActivity.class);
        if (locationCoords != null) {
            mapIntent.putExtra("lat", locationCoords.latitude);
            mapIntent.putExtra("lon", locationCoords.longitude);
        }
        startActivityForResult(mapIntent, REQUEST_LOCATION);
    }

    private void addListenersForPriceCalculation() {
        Log.d(TAG, "addListenersForPriceCalculation: adding TextWatcher for automatic calculations");
        fullPrice = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }


            @Override
            public void afterTextChanged(Editable s) {
                removeTextListener(0);
                if (inputL.getEditText().getText().toString().equals("")) {
                    addTextListener(0);
                    return;
                }
                if (s.toString().equals("")) {
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

        litrePrice = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @SuppressWarnings("ConstantConditions")
            @Override
            public void afterTextChanged(Editable s) {
                removeTextListener(1);
                if (inputL.getEditText().getText().toString().equals("")) {
                    addTextListener(1);
                    return;
                }
                if (s.toString().equals("")) {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null) {
                    timer.cancel();
                }
            }

            @SuppressWarnings("ConstantConditions")
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
                    } catch (Exception e) {
                        Log.e("AddNewDriveActivity", e.getMessage());
                    }
                    addTextListener(0);
                }
            }
        };

        //noinspection ConstantConditions
        inputPricePaid.getEditText().addTextChangedListener(fullPrice);
        //noinspection ConstantConditions
        inputLPrice.getEditText().addTextChangedListener(litrePrice);
        //noinspection ConstantConditions
        inputL.getEditText().addTextChangedListener(litres);

    }

    private void overrideTimeAndDateInputs() {
        Log.d(TAG, "overrideTimeAndDateInputs");
        /* open time dialog */
        //noinspection ConstantConditions
        inputTime.getEditText().setOnClickListener(v -> {
            MaterialTimePicker materialTimePicker = TimeDatePickerHelper.createTime(hidCalendar);
            materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            materialTimePicker.addOnPositiveButtonClickListener(v1 -> {
                Log.d(TAG, "on time change: " + materialTimePicker.getHour() + ":" + materialTimePicker.getMinute());
                hidCalendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
                hidCalendar.set(Calendar.MINUTE, materialTimePicker.getMinute());
                inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
            });
        });

        /* open date dialog */
        //noinspection ConstantConditions
        inputDate.getEditText().setOnClickListener(v -> {
            MaterialDatePicker<?> materialDatePicker = TimeDatePickerHelper.createDate(hidCalendar);
            materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                Log.d(TAG, "on date change: " + materialDatePicker.getHeaderText());
                Log.d(TAG, "on date change: " + Objects.requireNonNull(materialDatePicker.getSelection()).toString());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(materialDatePicker.getSelection().toString()));
                hidCalendar.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                hidCalendar.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                hidCalendar.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                String date = sdfDate.format(hidCalendar.getTime());
                inputDate.getEditText().setText(date);
            });
        });
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: registering new onClickListeners");
        setLocation.setOnClickListener(v -> startMap());
        inputLatitude.setOnClickListener(v -> startMap());
        inputLongitude.setOnClickListener(v -> startMap());

        firstFuel.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                firstFuelStatus = 1;
            else
                firstFuelStatus = 0;
        });

        notFull.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                notFullStatus = 1;
            else
                notFullStatus = 0;
        });


        /* save drive */
        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> addNewDrive());
    }

    /**
     * Removes textlistener when programmatically updating text
     * @param where boolean which to remove
     */
    @SuppressWarnings("ConstantConditions")
    private void removeTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().removeTextChangedListener(litrePrice);
        } else {
            inputPricePaid.getEditText().removeTextChangedListener(fullPrice);
        }
    }

    /**
     * Add textlistener when programmatically updating text finished
     * @param where boolean which to add back
     */
    @SuppressWarnings("ConstantConditions")
    private void addTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().addTextChangedListener(litrePrice);
        } else {
            inputPricePaid.getEditText().addTextChangedListener(fullPrice);
        }
    }

    /**
     * Create connection between fields and variables
     */
    private void initVariables() {
        Log.d(TAG, "initVariables: started");
        inputDate = findViewById(R.id.add_drive_date_input);
        inputTime = findViewById(R.id.add_drive_time_input);
        inputKm = findViewById(R.id.add_drive_km_input);
        inputL = findViewById(R.id.add_drive_litres_input);
        inputLPrice = findViewById(R.id.add_drive_price_per_l_input);
        inputPricePaid = findViewById(R.id.add_drive_total_cost_input);
        inputNote = findViewById(R.id.add_drive_note_input);
        inputLatitude = findViewById(R.id.add_drive_latitude_input);
        inputLongitude = findViewById(R.id.add_drive_longitude_input);

        inputDateEdit = findViewById(R.id.add_drive_date_input_edit);
        inputTimeEdit = findViewById(R.id.add_drive_time_input_edit);
        inputKmEdit = findViewById(R.id.add_drive_km_input_edit);
        inputLEdit = findViewById(R.id.add_drive_litres_input_edit);
        inputLPriceEdit = findViewById(R.id.add_drive_price_per_l_input_edit);
        inputPricePaidEdit = findViewById(R.id.add_drive_total_cost_input_edit);
        inputNoteEdit = findViewById(R.id.add_drive_note_input_edit);
        inputLatitudeEdit = findViewById(R.id.add_drive_latitude_input_edit);
        inputLongitudeEdit = findViewById(R.id.add_drive_longitude_input_edit);

        selectKm = findViewById(R.id.add_drive_km_mode_spinner);
        selectPetrolStationSpinner = findViewById(R.id.add_drive_petrol_station_spinner);
        selectCountry = findViewById(R.id.add_drive_country_spinner);

        petrolStationLogo = findViewById(R.id.add_drive_petrol_station_icon);
        firstFuel = findViewById(R.id.add_drive_first_fuelling);
        notFull = findViewById(R.id.add_drive_not_full);
        setLocation = findViewById(R.id.add_drive_manual_location);
        Log.d(TAG, "initVariables: finished");
    }

    /**
     * Set current date and time
     */
    @SuppressWarnings("ConstantConditions")
    private void fillVariables() {
        Log.d(TAG, "fillVariables: started");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Log.d(TAG, "fillVariables: setting calendar");
        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(hidCalendar.getTime()));

        Log.d(TAG, "fillVariables: setting petrol station spinner");
        SpinnerPetrolStationAdapter adapter = new SpinnerPetrolStationAdapter(this, dbHelper.getAllPetrolStations());
        selectPetrolStationSpinner.setAdapter(adapter);

        selectPetrolStationSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: petrol station changed to" + s.toString());
                if (s.toString().equals(getString(R.string.other))) {
                    Glide.with(getApplicationContext()).load(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_help_outline_black_24dp)).into(petrolStationLogo);
                } else {
                    String fileName = dbHelper.getPetrolStation(s.toString()).getFileName();
                    File storageDIR = getDir("Images", MODE_PRIVATE);
                    Glide.with(getApplicationContext()).load(storageDIR+"/"+fileName).diskCacheStrategy(DiskCacheStrategy.NONE).into(petrolStationLogo);
                }
            }
        });

        selectPetrolStationSpinner.setText(dbHelper.getPetrolStation(pref.getString("default_petrol_station", "Other")).getName(), false);

        Log.d(TAG, "fillVariables: setting country spinner");
        String[] countryCodes = Locale.getISOCountries();
        codes = new ArrayList<>();
        names = new ArrayList<>();
        for (String countryCode : countryCodes) {
            Locale obj = new Locale("", countryCode);
            codes.add(obj.getCountry());
            names.add(obj.getDisplayCountry(locale));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCountry.setAdapter(spinnerArrayAdapter);
        selectCountry.setTitle(getString(R.string.select_lang).split(" ")[0] + " " + getString(R.string.country).toLowerCase());

        if (dbHelper.getAllDrives(vehicleId) == null || dbHelper.getAllDrives(vehicleId).size() == 0) {
            firstFuel.setChecked(true);
            firstFuel.setEnabled(false);
            firstFuelStatus = 1;
        }

        Log.d(TAG, "fillVariables: import note if exists");
        String note = pref.getString("saved_note", "");

        if (!note.equals("")) {
            inputNote.getEditText().setText(note);
        }

        displayKmMode();

        selectCountry.setSelection(codes.indexOf("SI"));

        Log.d(TAG, "fillVariables: filling select km mode spinner");
        /* fill dropdown list */
        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.km_types, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectKm.setAdapter(adapterS);
        if (kmMode.equals(getString(R.string.total_meter)))
            selectKm.setSelection(0);
        else
            selectKm.setSelection(1);
        selectKm.setOnItemSelectedListener(this);


        Log.d(TAG, "fillVariables: finished");
    }

    /**
     * Validators for EditText fields
     */
    private void addValidators() {
        Log.d(TAG, "addValidators: adding validation and error for inputKM");
        inputKmEdit.addTextChangedListener(new EditTextWatcher(this, inputKm, inputKmEdit));
        inputLEdit.addTextChangedListener(new EditTextWatcher(this, inputL, inputLEdit));
        inputLPriceEdit.addTextChangedListener(new EditTextWatcher(this, inputLPrice, inputLPriceEdit));
        inputPricePaidEdit.addTextChangedListener(new EditTextWatcher(this, inputPricePaid, inputPricePaidEdit));
    }

    class EditTextWatcher implements TextWatcher{

        TextInputLayout layout;
        TextInputEditText edit;
        Context context;

        public EditTextWatcher(Context con, TextInputLayout layout, TextInputEditText edit){
            this.edit = edit;
            this.layout = layout;
            this.context = con;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (edit.getId()){
                case R.id.add_drive_km_input_edit:
                    Log.d(TAG, "afterTextChanged: add_drive_km_input_edit selected");
                    validateKilometres(layout, editable.toString());
                    break;
                case R.id.add_drive_litres_input_edit:
                case R.id.add_drive_price_per_l_input_edit:
                case R.id.add_drive_total_cost_input_edit:
                    Log.d(TAG, "afterTextChanged: add_drive_(litres/price_per_l/total_cost)_input_edit selected");
                    validateIsNotEmpty(layout, editable.toString());
                    break;
            }
        }
    }

    private boolean validateKilometres(TextInputLayout layout, @NonNull String value) {
        if (kmMode.equals(getString(R.string.total_meter))) {
            if (value.equals("")) {
                if (layout.getError() == null || !layout.getError().toString().equals("Field cannot be empty!")) {
                    Log.d(TAG, "afterTextChanged: setting new error");
                    layout.setError("Field cannot be empty!");
                }
                return false;
            } else if (vo.getOdoFuelKm() > Integer.parseInt(value)) {
                if (layout.getError() == null || !layout.getError().toString().equals("Kilometres should be higher!")) {
                    Log.d(TAG, "afterTextChanged: setting new error");
                    layout.setError("Kilometres should be higher!");
                }
                return false;
            } else {
                Log.d(TAG, "afterTextChanged: resetting error");
                layout.setError(null);
                displayPrevKm();
                return true;
            }
        } else {
            if (validateIsNotEmpty(layout, value)) {
                displayPrevKm();
                return true;
            }
            return false;
        }
    }

    private boolean validateIsNotEmpty(TextInputLayout layout, @NonNull String value) {
        if (value.equals("")) {
            if (layout.getError() == null || !layout.getError().toString().equals("Field cannot be empty!")) {
                Log.d(TAG, "afterTextChanged: setting new error");
                layout.setError("Field cannot be empty!");
            }
            return false;
        } else {
            Log.d(TAG, "afterTextChanged: resetting error");
            layout.setError(null);
            return true;
        }
    }

    /**
     * Saves new drive
     */
    //@SuppressWarnings("ConstantConditions")
    private void addNewDrive() {
        Log.d(TAG, "addNewDrive: started");
        final DriveObject driveObject = new DriveObject();
        boolean error = false;

        Log.d(TAG, "addNewDrive: saving new drive in mode: " + kmMode);

        driveObject.setCarID(vehicleId);

        if (!validateIsNotEmpty(inputL, inputLEdit.getText().toString())) {
            error = true;
        } else {
            driveObject.setLitres(inputLEdit.getText().toString());
        }

        if (!validateIsNotEmpty(inputLPrice, inputLPriceEdit.getText().toString())) {
            error = true;
        } else {
            driveObject.setCostPerLitre(inputLPriceEdit.getText().toString());
        }

        if (!validateIsNotEmpty(inputPricePaid, inputPricePaidEdit.getText().toString())) {
            error = true;
        } else {
            driveObject.setCostPerLitre(inputPricePaidEdit.getText().toString());
        }

        driveObject.setDate(hidCalendar);

        if (!validateKilometres(inputKm, inputKmEdit.getText().toString())) {
            error = true;
        }

        /* throw visual errors */
        if (error) {
            Log.d(TAG, "addNewDrive: one or more values are missing");
            Toast.makeText(this, getString(R.string.fill_text_cost), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "addNewDrive: all values are correct");

        driveObject.setFirst(firstFuelStatus);
        driveObject.setNotFull(notFullStatus);

        final int displayKm = Integer.parseInt(inputKmEdit.getText().toString());
        DriveObject prevDrive = dbHelper.getPrevDrive(vehicleId);
        String stringNote = inputNote.getEditText().getText().toString();
        if (stringNote.length() == 0)
            stringNote = null;
        driveObject.setNote(stringNote);

        String station = selectPetrolStationSpinner.getText().toString();
        driveObject.setPetrolStation(station);
        driveObject.setCountry(codes.get(names.indexOf(selectCountry.getSelectedItem().toString())));

        if (locationCoords != null) {
            Log.d(TAG, "addNewDrive: setting coordinates");
            driveObject.setLatitude(locationCoords.latitude);
            driveObject.setLongitude(locationCoords.longitude);
        }

        if (kmMode.equals(getString(R.string.total_meter))) {
            Log.d(TAG, "addNewDrive: entered km are odo");
            if (vo.getOdoFuelKm() > displayKm) {
                //should be caught before
            }
            if (prevDrive == null) {
                //the first
                Log.d(TAG, "addNewDrive: this is the first drive");
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - vo.getOdoFuelKm());
                vo.setOdoFuelKm(displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else if (hidCalendar.getTimeInMillis() < prevDrive.getDateEpoch() * 1000) {
                Log.e(TAG, "addNewDrive: kilometres are bigger, time is smaller than prev");
                inputKm.setError("Mode ODO is not supported for older dates.");
                Toast.makeText(this, R.string.drive_not_inserted_between, Toast.LENGTH_LONG).show();
                return;
            } else {
                Log.d(TAG, "addNewDrive: adding new drive");
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - vo.getOdoFuelKm());
                vo.setOdoFuelKm(displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            }
        } else {
            Log.d(TAG, "addNewDrive: entered km are trip");
            if (prevDrive == null) {
                //the first
                Log.d(TAG, "addNewDrive: this is the first drive");
                driveObject.setOdo(vo.getOdoFuelKm() + displayKm);
                driveObject.setTrip(displayKm);
                vo.setOdoFuelKm(vo.getOdoFuelKm() + displayKm);
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else if (hidCalendar.getTimeInMillis() < prevDrive.getDateEpoch() * 1000) {
                Log.d(TAG, "addNewDrive: time is smaller than prev");
                DriveObject biggest = dbHelper.getLastDrive(vehicleId);
                List<DriveObject> newer = dbHelper.getAllDrivesWhereTimeBetween(vehicleId, hidCalendar.getTimeInMillis() / 1000 + 10, biggest.getDateEpoch() + 10);
                int sumTrip = 0;
                for (DriveObject drive : newer) {
                    int newOdo = drive.getOdo() + displayKm;
                    sumTrip += drive.getTrip();
                    drive.setOdo(newOdo);
                    dbHelper.updateDriveODO(drive);
                }
                driveObject.setOdo(vo.getOdoFuelKm() - sumTrip + displayKm);
                driveObject.setTrip(displayKm);
                vo.setOdoFuelKm(vo.getOdoFuelKm() + displayKm);
                Log.d(TAG, "addNewDrive: adding new drive");
                dbHelper.updateVehicle(vo);
                dbHelper.addDrive(driveObject);
            } else {
                Log.d(TAG, "addNewDrive: adding new drive");
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

        Log.d(TAG, "addNewDrive: finished");
        Utils.checkKmAndSetAlarms(vehicleId, dbHelper, this);
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
        displayKmMode();
        displayPrevKm();
    }

    /**
     * Display chosen km mode
     */
    private void displayKmMode() {
        if (kmMode.equals(getString(R.string.total_meter)))
            inputKm.setHint(getString(R.string.total_meter));
        else
            inputKm.setHint(getString(R.string.trip_meter).substring(0, 1).toUpperCase() + getString(R.string.trip_meter).substring(1));
    }

    /**
     * Display previous drive odo
     */
    private void displayPrevKm() {
        inputKm.setHelperText(String.format(locale, "odo: %dkm", vo.getOdoFuelKm()));
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("km", (inputKm.getEditText().getText().toString()));
        outState.putString("litre", (inputL.getEditText().getText().toString()));
        outState.putString("price_litre", (inputLPrice.getEditText().getText().toString()));
        outState.putString("price", (inputPricePaid.getEditText().getText().toString()));
        outState.putString("note", inputNote.getEditText().getText().toString());

        outState.putString("date", sdfDate.format(hidCalendar.getTime()));
        outState.putString("time", sdfTime.format(hidCalendar.getTime()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        inputKm.getEditText().setText(savedInstanceState.getString("km"));
        inputL.getEditText().setText(savedInstanceState.getString("litre"));
        inputLPrice.getEditText().setText(savedInstanceState.getString("price_litre"));
        inputPricePaid.getEditText().setText(savedInstanceState.getString("price"));
        inputNote.getEditText().setText(savedInstanceState.getString("note"));

        String date = savedInstanceState.getString("date");
        String time = savedInstanceState.getString("time");

        inputDate.getEditText().setText(date);
        inputTime.getEditText().setText(time);

        String[] dateS = date.split("\\.");
        String[] timeS = time.split(":");
        hidCalendar.set(Integer.parseInt(dateS[2]), Integer.parseInt(dateS[1]),
                Integer.parseInt(dateS[0]), Integer.parseInt(timeS[0]),
                Integer.parseInt(timeS[1]));
    }


    /*
    GPS stuff
     */

    private void checkGPSPermissions() {
        if (EasyPermissions.hasPermissions(this, PERMISSIONS_LOCATION))
            //start async get location
            getLocationService();
        else
            EasyPermissions.requestPermissions(this, getString(R.string.why_location),
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
        Log.d(TAG, "onPermissionsDenied: " + requestCode + ":" + perms.size());
        inputLatitude.setHint(getString(R.string.disabled_gps));
        inputLongitude.setHint(getString(R.string.disabled_gps));
        /*if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }*/
    }

    @SuppressWarnings("ConstantConditions")
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
                    Log.d(TAG, "onActivityResult: GPS Enabled by user");
                    getOneLocationUpdate();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "onActivityResult: User rejected GPS request");
                    inputLatitude.setHint(getString(R.string.disabled_gps));
                    inputLongitude.setHint(getString(R.string.disabled_gps));
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_LOCATION) {
            if (resultCode == RESULT_OK) {
                //change to new location
                inputLatitude.setHint(getString(R.string.latitude));
                inputLongitude.setHint(getString(R.string.longitude));
                inputLatitude.getEditText().setText(String.format(locale, "%f", data.getDoubleExtra("lat", 0)));
                inputLongitude.getEditText().setText(String.format(locale, "%f", data.getDoubleExtra("lon", 0)));
                locationCoords = new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lon", 0));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void getLocationService() {
        client = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000);
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
                Log.d(TAG, "onSuccess: location is already enabled");
                getOneLocationUpdate();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.d(TAG, "onFailure: location is not (yet) enabled");
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
                if (locationResult == null) {
                    selectCountry.setSelection(codes.indexOf("SI"));
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        inputLatitude.setHint(getString(R.string.latitude));
                        inputLongitude.setHint(getString(R.string.longitude));
                        inputLatitude.getEditText().setText(String.format(locale, "%f", location.getLatitude()));
                        inputLongitude.getEditText().setText(String.format(locale, "%f", location.getLongitude()));
                        lastLocation = location;
                        locationCoords = new LatLng(location.getLatitude(), location.getLongitude());
                        if (client != null) {
                            client.removeLocationUpdates(locationCallback);
                        }
                        Geocoder geocoder = new Geocoder(getApplicationContext(), locale);
                        Address address = null;
                        try {
                            address = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1).get(0);
                        } catch (IOException e) {
                            Log.e(TAG, "onLocationResult: ", e);
                        }
                        selectCountry.setSelection(codes.indexOf(address.getCountryCode()));
                    }
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void getOneLocationUpdate() {
        //when code comes to here permissions are already granted with EasyPermission
        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}

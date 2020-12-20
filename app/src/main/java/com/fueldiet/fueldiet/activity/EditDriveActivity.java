package com.fueldiet.fueldiet.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.adapter.SpinnerPetrolStationAdapter;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.TimeDatePickerHelper;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;

public class EditDriveActivity extends BaseActivity {
    private static final String TAG = "EditDriveActivity";

    private long vehicleId;
    private long driveId;
    private static final int REQUEST_LOCATION = 1324;
    private FuelDietDBHelper dbHelper;
    private VehicleObject vehicleObject;

    private Spinner selectKm;
    private TextInputLayout inputDate, inputTime, inputKm, inputL, inputLPrice, inputPricePaid, inputNote, inputLatitude, inputLongitude;
    private TextInputEditText inputDateEdit, inputTimeEdit, inputKmEdit, inputLEdit, inputLPriceEdit, inputPricePaidEdit, inputNoteEdit, inputLatitudeEdit, inputLongitudeEdit;
    private AutoCompleteTextView selectPetrolStationSpinner;
    private SearchableSpinner selectCountry;
    private MaterialButton setLocation;
    private ImageView petrolStationLogo;

    private SwitchMaterial firstFuel, notFull;
    private int firstFuelStatus;
    private int notFullStatus;

    private List<String> codes;
    private List<String> names;

    SimpleDateFormat sdfDate, sdfTime;

    TextWatcher fullPrice, litrePrice, litres;

    private DriveObject old;
    private Calendar changedCal;
    private int newOdo;
    Timer timer;
    private LatLng locationCoords;
    Locale locale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);
        Log.d(TAG, "onCreate: started");

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setTitle(R.string.edit_drive_title);

        Intent intent = getIntent();
        driveId = intent.getLongExtra("drive_id", 1);
        vehicleId = intent.getLongExtra("vehicle_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        vehicleObject = dbHelper.getVehicle(vehicleId);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        initVariables();
        fillFields();
        addListeners();
        overrideTimeAndDateInputs();
        setOnClickListeners();
        setOnCheckListeners();
        addValidators();
    }

    private void startMap() {
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra("lat", locationCoords.latitude);
        mapIntent.putExtra("lon", locationCoords.longitude);
        startActivityForResult(mapIntent, REQUEST_LOCATION);
    }

    private void addListeners() {
        fullPrice = new TextWatcher() {
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

        litrePrice = new TextWatcher() {
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
                double lprice = Double.parseDouble(s.toString());
                double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                inputPricePaid.getEditText().setText(String.valueOf(Utils.calculateFullPrice(lprice, litres)));
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
                    double lprice = Double.parseDouble(inputLPrice.getEditText().getText().toString());
                    double litres = Double.parseDouble(s.toString());
                    inputPricePaid.getEditText().setText(String.valueOf(Utils.calculateFullPrice(lprice, litres)));
                    addTextListener(1);
                } else if (!inputPricePaid.getEditText().getText().toString().equals("")) {
                    removeTextListener(0);
                    double total = Double.parseDouble(s.toString());
                    double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                    inputLPrice.getEditText().setText(String.valueOf(Utils.calculateLitrePrice(total, litres)));
                    addTextListener(0);
                }
            }
        };

        inputPricePaid.getEditText().addTextChangedListener(fullPrice);
        inputLPrice.getEditText().addTextChangedListener(litrePrice);
        inputL.getEditText().addTextChangedListener(litres);
    }

    private void overrideTimeAndDateInputs() {
        Log.d(TAG, "overrideTimeAndDateInputs");
        /* Open time/date dialog */
        inputTime.getEditText().setOnClickListener(v -> {
            MaterialTimePicker materialTimePicker = TimeDatePickerHelper.createTime(changedCal);
            materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            materialTimePicker.addOnPositiveButtonClickListener(v1 -> {
                Log.d(TAG, "on time change: " + materialTimePicker.getHour() + ":" + materialTimePicker.getMinute());
                changedCal.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
                changedCal.set(Calendar.MINUTE, materialTimePicker.getMinute());
                inputTime.getEditText().setText(sdfTime.format(changedCal.getTime()));
            });
        });

        /* Open time/date dialog */
        inputDate.getEditText().setOnClickListener(v -> {
            MaterialDatePicker<?> materialDatePicker = TimeDatePickerHelper.createDate(changedCal);
            materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                Log.d(TAG, "on date change: " + materialDatePicker.getHeaderText());
                Log.d(TAG, "on date change: " + Objects.requireNonNull(materialDatePicker.getSelection()).toString());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(materialDatePicker.getSelection().toString()));
                changedCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                changedCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                changedCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                String date = sdfDate.format(changedCal.getTime());
                inputDate.getEditText().setText(date);
            });
        });
    }

    private void setOnClickListeners() {
        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> saveEditDrive());
        inputLatitude.setOnClickListener(v -> startMap());
        inputLongitude.setOnClickListener(v -> startMap());
        setLocation.setOnClickListener(v -> startMap());
    }

    private void setOnCheckListeners() {
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
    }

    /**
     * Validators for EditText fields
     */
    private void addValidators() {
        Log.d(TAG, "addValidators: adding validation and error for inputKM");
        inputKmEdit.addTextChangedListener(new EditDriveActivity.EditTextWatcher(this, inputKm, inputKmEdit));
        inputLEdit.addTextChangedListener(new EditDriveActivity.EditTextWatcher(this, inputL, inputLEdit));
        inputLPriceEdit.addTextChangedListener(new EditDriveActivity.EditTextWatcher(this, inputLPrice, inputLPriceEdit));
        inputPricePaidEdit.addTextChangedListener(new EditDriveActivity.EditTextWatcher(this, inputPricePaid, inputPricePaidEdit));
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
                    if (validateIsNotEmpty(layout, editable.toString())) {
                        newOdo = old.getOdo() - old.getTrip() + Integer.parseInt(inputKm.getEditText().getText().toString());
                        displayKmOdo();
                    }
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
     * Removes textlistener when programmatically updating text
     * @param where boolean which to remove
     */
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

        old = dbHelper.getDrive(driveId);
        inputLatitude = findViewById(R.id.add_drive_latitude_input);
        inputLongitude = findViewById(R.id.add_drive_longitude_input);
        setLocation = findViewById(R.id.add_drive_manual_location);
        Log.d(TAG, "initVariables: finished");
    }

    /**
     * Create connection between fields and variables
     */
    @SuppressWarnings("ConstantConditions")
    private void fillFields() {
        Log.d(TAG, "fillVariables: started");
        newOdo = old.getOdo();
        changedCal = old.getDate();

        Log.d(TAG, "fillVariables: setting calendar");
        inputTime.getEditText().setText(sdfTime.format(old.getDate().getTime()));
        inputDate.getEditText().setText(sdfDate.format(old.getDate().getTime()));

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
        selectPetrolStationSpinner.setText(old.getPetrolStation(), false);

        Log.d(TAG, "fillVariables: setting country spinner");
        String[] countryCodes = Locale.getISOCountries();
        codes = new ArrayList<>();
        names = new ArrayList<>();
        for (String countryCode : countryCodes) {
            Locale obj = new Locale("", countryCode);
            codes.add(obj.getCountry());
            names.add(obj.getDisplayCountry(locale));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCountry.setAdapter(spinnerArrayAdapter);

        selectCountry.setSelection(codes.indexOf(old.getCountry()));

        Log.d(TAG, "fillFields: displaying km");
        displayKmMode();
        displayKmOdo();
        inputKm.getEditText().setText(String.format(locale, "%d", old.getTrip()));

        Log.d(TAG, "fillFields: displaying litres and prices");
        inputL.getEditText().setText(String.format(locale, "%.3f", old.getLitres()));
        inputLPrice.getEditText().setText(String.format(locale, "%.3f",old.getCostPerLitre()));
        inputPricePaid.getEditText().setText(
                String.format(locale, "%.3f",Utils.calculateFullPrice(old.getCostPerLitre(), old.getLitres())));

        Log.d(TAG, "fillFields: displaying note");
        String note = old.getNote();
        if (note != null && note.length() != 0)
            inputNote.getEditText().setText(note);

        Log.d(TAG, "fillFields: displaying other setting");
        firstFuel.setChecked(old.getFirst() == 1);
        firstFuelStatus = old.getFirst();
        notFull.setChecked(old.getNotFull() == 1);
        notFullStatus = old.getNotFull();

        Log.d(TAG, "fillFields: displaying location");
        Double lat = old.getLatitude();
        Double longi = old.getLongitude();
        if (lat != null && longi != null && lat != 0.0 && longi != 0.0) {
            inputLatitude.setHint(getString(R.string.latitude));
            inputLongitude.setHint(getString(R.string.longitude));
            inputLatitude.getEditText().setText(String.format(locale, "%f", lat));
            inputLongitude.getEditText().setText(String.format(locale, "%f", longi));
            locationCoords = new LatLng(lat, longi);
        } else {
            inputLatitude.setHint(getString(R.string.disabled_gps));
            inputLongitude.setHint(getString(R.string.disabled_gps));
            locationCoords = null;
        }
    }

    /**
     * Display previous drive odo
     */
    private void displayKmMode() {
        inputKm.setHint(getString(R.string.trip_meter));
        selectKm.setEnabled(false);
    }

    /**
     * Display chosen km mode
     */
    private void displayKmOdo() {
        inputKm.setHelperText(String.format(locale, "%s odo: %dkm, %s odo: %dkm",
                getString(R.string.old_km), old.getOdo(), getString(R.string.new_km), newOdo));
    }

    private void saveEditDrive() {
        Log.d(TAG, "saveEditDrive: started");
        final DriveObject driveObject = new DriveObject();
        boolean error = false;


        driveObject.setOdo(newOdo);
        driveObject.setId(driveId);
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
        }

        driveObject.setDate(changedCal);

        if (!validateIsNotEmpty(inputKm, inputKmEdit.getText().toString())) {
            error = true;
        } else {
            driveObject.setTrip(inputKmEdit.getText().toString());
        }

        /* throw visual errors */
        if (error) {
            Log.d(TAG, "saveEditDrive: one or more values are missing");
            Toast.makeText(this, getString(R.string.fill_text_cost), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "saveEditDrive: all values are correct");

        String note = inputNote.getEditText().getText().toString();
        if (note.length() == 0)
            note = null;
        driveObject.setNote(note);

        if (locationCoords != null) {
            driveObject.setLatitude(locationCoords.latitude);
            driveObject.setLongitude(locationCoords.longitude);
        }

        driveObject.setPetrolStation(selectPetrolStationSpinner.getText().toString());
        driveObject.setCountry(codes.get(names.indexOf(selectCountry.getSelectedItem().toString())));
        driveObject.setFirst(firstFuelStatus);
        driveObject.setNotFull(notFullStatus);

        DriveObject prevDrive = dbHelper.getPrevDriveSelection(vehicleId, old.getDateEpoch());
        DriveObject nextDrive = dbHelper.getNextDriveSelection(vehicleId, old.getDateEpoch());

        if (prevDrive == null && nextDrive == null) {
            dbHelper.updateDriveODO(driveObject);
        } else if (prevDrive == null) {
            if (changedCal.before(nextDrive.getDate())) {
                dbHelper.updateDriveODO(driveObject);
            } else {
                Toast.makeText(getApplicationContext(), "Date is after next entry", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (nextDrive == null) {
            if (changedCal.after(prevDrive.getDate())) {
                dbHelper.updateDriveODO(driveObject);
            } else {
                Toast.makeText(getApplicationContext(), "Date is before prev entry", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (changedCal.after(nextDrive.getDate())) {
                Toast.makeText(getApplicationContext(), "Date is after next entry", Toast.LENGTH_SHORT).show();
                return;
            } else if (changedCal.before(prevDrive.getDate())) {
                Toast.makeText(getApplicationContext(), "Date is before prev entry", Toast.LENGTH_SHORT).show();
                return;
            } else {
                dbHelper.updateDriveODO(driveObject);
            }
        }
        DriveObject biggest = dbHelper.getLastDrive(vehicleId);
        List<DriveObject> oldDrives = dbHelper.getAllDrivesWhereTimeBetween(vehicleId, (changedCal.getTimeInMillis()/1000)+10, biggest.getDateEpoch()+10);
        int diffOdo = newOdo - old.getOdo();

        vehicleObject.setOdoFuelKm(vehicleObject.getOdoFuelKm() + diffOdo);
        dbHelper.updateVehicle(vehicleObject);

        for (DriveObject driveBigger : oldDrives) {
            int newOdo = driveBigger.getOdo() + diffOdo;
            driveBigger.setOdo(newOdo);
            dbHelper.updateDriveODO(driveBigger);
        }
        Utils.checkKmAndSetAlarms(vehicleId, dbHelper, this);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION) {
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
}

package com.fueldiet.fueldiet.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.adapter.SpinnerPetrolStationAdapter;
import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class EditDriveActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private long vehicleID;
    private long driveID;
    private FuelDietDBHelper dbHelper;
    private VehicleObject vehicleObject;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private Spinner selectKM;
    private TextInputLayout inputKM;
    private TextView prevKM;

    private TextInputLayout inputL;
    private TextInputLayout inputLPrice;
    private TextInputLayout inputPricePaid;
    private TextInputLayout inputNote;
    private TextInputLayout inputGPS;
    private Spinner selectPetrolStation;
    private Spinner selectCountry;

    private Switch firstFuel;
    private Switch notFull;
    private int firstFuelStatus;
    private int notFullStatus;

    private List<String> codes;
    private List<String> names;

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    TextWatcher fullprice;
    TextWatcher litreprice;
    TextWatcher litres;
    TextWatcher km;

    private DriveObject old;
    private Calendar changedCal;
    private int newOdo;
    Timer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_drive_title);

        Intent intent = getIntent();
        driveID = intent.getLongExtra("drive_id", (long)1);
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        vehicleObject = dbHelper.getVehicle(vehicleID);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        initVariables();
        fillFields();

        /* Open time/date dialog */
        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", changedCal.getTimeInMillis());
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.setArguments(currentDate);
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        /* Open time/date dialog */
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", changedCal.getTimeInMillis());
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
        km = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (inputKM.getEditText().getText().toString().equals("") ||
                        Integer.parseInt(inputKM.getEditText().getText().toString()) < 1) {
                    inputKM.getEditText().setText("1");
                    Toast.makeText(getApplicationContext(), "New trip cannot be smaller than 1km.", Toast.LENGTH_SHORT).show();
                } else {
                    newOdo = old.getOdo() - old.getTrip() + Integer.parseInt(inputKM.getEditText().getText().toString());
                    displayKModo();
                }
            }
        };

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

        inputPricePaid.getEditText().addTextChangedListener(fullprice);
        inputLPrice.getEditText().addTextChangedListener(litreprice);
        inputL.getEditText().addTextChangedListener(litres);
        inputKM.getEditText().addTextChangedListener(km);

        /* save edited drive */
        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> saveEditDrive());
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

        old = dbHelper.getDrive(driveID);
        newOdo = old.getOdo();
        changedCal = old.getDate();
        inputGPS = findViewById(R.id.add_drive_gps_input);
    }

    /**
     * Create connection between fields and variables
     */
    private void fillFields() {
        inputTime.getEditText().setText(sdfTime.format(old.getDate().getTime()));
        inputDate.getEditText().setText(sdfDate.format(old.getDate().getTime()));
        displayKMmode();
        displayKModo();
        inputKM.getEditText().setText(old.getTrip()+"");
        inputL.getEditText().setText(String.valueOf(old.getLitres()));
        inputLPrice.getEditText().setText(String.valueOf(old.getCostPerLitre()));
        inputPricePaid.getEditText().setText(
                String.valueOf(Utils.calculateFullPrice(old.getCostPerLitre(), old.getLitres())));
        String note = old.getNote();
        if (note != null && note.length() != 0)
            inputNote.getEditText().setText(note);

        SpinnerPetrolStationAdapter adapter = new SpinnerPetrolStationAdapter(this, getResources().getStringArray(R.array.petrol_stations));
        selectPetrolStation.setAdapter(adapter);
        selectPetrolStation.setSelection(adapter.getPosition(old.getPetrolStation()));

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

        selectCountry.setSelection(codes.indexOf(old.getCountry()));

        firstFuel.setChecked(old.getFirst() == 1);
        firstFuelStatus = old.getFirst();
        notFull.setChecked(old.getNotFull() == 1);
        notFullStatus = old.getNotFull();

        String gps = old.getGpsLocation();
        if (gps != null && gps.length() != 0) {
            inputGPS.getEditText().setText(gps);
            inputGPS.setHint(getString(R.string.gps_location));
        } else {
            inputGPS.setHint(getString(R.string.disabled_gps));
        }
    }

    /**
     * Display previous drive odo
     */
    private void displayKMmode() {
        inputKM.setHint(getString(R.string.trip_meter));
        selectKM.setEnabled(false);
    }

    /**
     * Display chosen km mode
     */
    private void displayKModo() {
        prevKM.setText(String.format("%s odo: %dkm, %s odo: %dkm",
                getString(R.string.old_km), old.getOdo(), getString(R.string.new_km), newOdo));
    }

    private void saveEditDrive() {
        final DriveObject driveObject = new DriveObject();
        boolean ok = true;

        driveObject.setOdo(newOdo);
        driveObject.setId(driveID);
        ok = ok && driveObject.setTrip(inputKM.getEditText().getText().toString());
        ok = ok && driveObject.setCarID(vehicleID);
        ok = ok && driveObject.setCostPerLitre(inputLPrice.getEditText().getText().toString());
        ok = ok && driveObject.setLitres(inputL.getEditText().getText().toString());
        ok = ok && driveObject.setDate(changedCal);

        if (!ok) {
            Toast.makeText(this, getString(R.string.fill_text_cost), Toast.LENGTH_LONG).show();
            return;
        }

        String note = inputNote.getEditText().getText().toString();
        if (note == null || note.length() == 0)
            note = null;
        driveObject.setNote(note);
        String gps = inputGPS.getEditText().getText().toString();
        if (gps == null || gps.length() == 0)
            gps = null;
        driveObject.setGpsLocation(gps);

        driveObject.setPetrolStation(Utils.fromSLOtoENG(selectPetrolStation.getSelectedItem().toString()));
        driveObject.setCountry(codes.get(names.indexOf(selectCountry.getSelectedItem().toString())));
        driveObject.setFirst(firstFuelStatus);
        driveObject.setNotFull(notFullStatus);

        DriveObject prevDrive = dbHelper.getPrevDriveSelection(vehicleID, old.getDateEpoch());
        DriveObject nextDrive = dbHelper.getNextDriveSelection(vehicleID, old.getDateEpoch());

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
        DriveObject biggest = dbHelper.getLastDrive(vehicleID);
        List<DriveObject> oldDrives = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, (changedCal.getTimeInMillis()/1000)+10, biggest.getDateEpoch()+10);
        int diffOdo = newOdo - old.getOdo();

        vehicleObject.setOdoFuelKm(vehicleObject.getOdoFuelKm() + diffOdo);
        dbHelper.updateVehicle(vehicleObject);

        for (DriveObject driveBigger : oldDrives) {
            int newOdo = driveBigger.getOdo() + diffOdo;
            driveBigger.setOdo(newOdo);
            dbHelper.updateDriveODO(driveBigger);
        }
        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        finish();

    }

    /**
     * Updates calendar with new time
     * @param view view
     * @param hourOfDay selected hour
     * @param minute selected minutes
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        changedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        changedCal.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(changedCal.getTime()));
    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
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
        changedCal.set(Calendar.YEAR, year);
        changedCal.set(Calendar.MONTH, month);
        changedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(changedCal.getTime());
        inputDate.getEditText().setText(date);
    }
}

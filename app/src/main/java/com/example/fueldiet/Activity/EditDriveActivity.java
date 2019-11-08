package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

public class EditDriveActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private long vehicleID;
    private long driveID;
    private FuelDietDBHelper dbHelper;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private Spinner selectKM;
    private TextInputLayout inputKM;
    private TextView prevKM;

    private TextInputLayout inputL;
    private TextInputLayout inputLPrice;
    private TextInputLayout inputPricePaid;

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    TextWatcher fullprice;
    TextWatcher litreprice;
    TextWatcher litres;
    TextWatcher km;

    private VehicleObject vo;
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

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        initVariable();
        fillVariable();

        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", changedCal.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", changedCal.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

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
                    displaKModo();
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


        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> saveEditDrive());
    }

    private void removeTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().removeTextChangedListener(litreprice);
        } else {
            inputPricePaid.getEditText().removeTextChangedListener(fullprice);
        }
    }

    private void addTextListener(int where) {
        if (where == 0) {
            inputLPrice.getEditText().addTextChangedListener(litreprice);
        } else {
            inputPricePaid.getEditText().addTextChangedListener(fullprice);
        }
    }

    private void initVariable() {
        inputDate = findViewById(R.id.add_drive_date_input);
        inputTime = findViewById(R.id.add_drive_time_input);

        inputKM = findViewById(R.id.add_drive_km_input);
        selectKM = findViewById(R.id.add_drive_km_mode_spinner);
        prevKM = findViewById(R.id.add_drive_prev_km);

        inputL = findViewById(R.id.add_drive_litres_input);
        inputLPrice = findViewById(R.id.add_drive_price_per_l_input);
        inputPricePaid = findViewById(R.id.add_drive_total_cost_input);

        vo = dbHelper.getVehicle(vehicleID);
        old = dbHelper.getDrive(driveID);
        newOdo = old.getOdo();
        changedCal = old.getDate();
    }

    private void fillVariable() {
        inputTime.getEditText().setText(sdfTime.format(old.getDate().getTime()));
        inputDate.getEditText().setText(sdfDate.format(old.getDate().getTime()));
        displayKMmode();
        displaKModo();
        inputKM.getEditText().setText(old.getTrip()+"");
        inputL.getEditText().setText(String.valueOf(old.getLitres()));
        inputLPrice.getEditText().setText(String.valueOf(old.getCostPerLitre()));
        inputPricePaid.getEditText().setText(
                String.valueOf(Utils.calculateFullPrice(old.getCostPerLitre(), old.getLitres())));
    }

    private void displayKMmode() {
        inputKM.setHint(getString(R.string.trip_meter));
        selectKM.setEnabled(false);
    }

    private void displaKModo() {
        prevKM.setText(String.format("old odo: %dkm, new odo: %dkm",
                old.getOdo(), newOdo));
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
        DriveObject prevDrive = dbHelper.getPrevDriveSelection(vehicleID, old.getOdo());
        DriveObject nextDrive = dbHelper.getNextDriveSelection(vehicleID, old.getOdo());

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

        for (DriveObject driveBigger : oldDrives) {
            int newOdo = driveBigger.getOdo() + diffOdo;
            driveBigger.setOdo(newOdo);
            dbHelper.updateDriveODO(driveBigger);
        }

        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        Intent intent = new Intent(EditDriveActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        startActivity(intent);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        //calendar.set(Calendar.MINUTE, minute);
        changedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        changedCal.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(changedCal.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        /*Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
         */
        changedCal.set(Calendar.YEAR, year);
        changedCal.set(Calendar.MONTH, month);
        changedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(changedCal.getTime());
        inputDate.getEditText().setText(date);
    }
}

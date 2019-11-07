package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.Timer;

public class AddNewDriveActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private enum KilometresMode {
        ODO, TRIP
    }

    private long vehicleID;
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
    private KilometresMode kmMode;

    TextWatcher fullprice;
    TextWatcher litreprice;
    TextWatcher litres;

    private VehicleObject vo;
    Timer timer;

    /*
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        vo = dbHelper.getVehicle(vehicleID);

        kmMode = KilometresMode.ODO;
        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        initVariable();
        fillVariable();

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.km_types, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectKM.setAdapter(adapterS);
        selectKM.setSelection(0);
        selectKM.setOnItemSelectedListener(this);

        inputTime.getEditText().setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.getEditText().setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

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


        FloatingActionButton addVehicle = findViewById(R.id.add_drive_save);
        addVehicle.setOnClickListener(v -> addNewDrive());
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
    }

    private void fillVariable() {
        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));
        displayKMmode();
    }

    private void addNewDrive() {
        final DriveObject driveObject = new DriveObject();
        boolean ok = true;

        //ok = ok && driveObject.setOdo(inputKM.getEditText().getText().toString());
        ok = ok && driveObject.setCarID(vehicleID);
        ok = ok && driveObject.setCostPerLitre(inputLPrice.getEditText().getText().toString());
        ok = ok && driveObject.setLitres(inputL.getEditText().getText().toString());

        Calendar c = Calendar.getInstance();
        String [] date = inputDate.getEditText().getText().toString().split("\\.");
        String [] time = inputTime.getEditText().getText().toString().split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        ok = ok && driveObject.setDate(c);
        String displayStringKm = inputKM.getEditText().getText().toString();

        if (!ok || displayStringKm.equals("")) {
            Toast.makeText(this, getString(R.string.fill_text_cost), Toast.LENGTH_LONG).show();
            return;
        }

        final int displayKm = Integer.parseInt(displayStringKm);

        DriveObject prevDrive = dbHelper.getPrevDrive(vehicleID);

        if (kmMode == KilometresMode.ODO) {
            if (prevDrive != null && prevDrive.getOdo() > displayKm) {
                Toast.makeText(this, getString(R.string.km_is_smaller_than_prev), Toast.LENGTH_SHORT).show();
                return;
            }
            if (prevDrive == null) {
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - vo.getInitKM());
                dbHelper.addDrive(driveObject);
            } else if (c.getTimeInMillis() < driveObject.getDateEpoch()*1000) {
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            } else {
                driveObject.setOdo(displayKm);
                driveObject.setTrip(displayKm - prevDrive.getOdo());
                dbHelper.addDrive(driveObject);
            }
        } else {
            if (prevDrive == null) {
                driveObject.setOdo(vo.getInitKM() + displayKm);
                driveObject.setTrip(displayKm);
                dbHelper.addDrive(driveObject);
            } else if (c.getTimeInMillis() < prevDrive.getDateEpoch()*1000) {
                Toast.makeText(this, getString(R.string.time_is_before_prev), Toast.LENGTH_SHORT).show();
                return;
            } else {
                driveObject.setOdo(prevDrive.getOdo() + displayKm);
                driveObject.setTrip(displayKm);
                dbHelper.addDrive(driveObject);
            }
        }
        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        Intent intent = new Intent(AddNewDriveActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        startActivity(intent);
    }

    private void changeKMmode(int position) {
        switch (position) {
            case 0:
                kmMode = KilometresMode.ODO;

                break;
            case 1:
                kmMode = KilometresMode.TRIP;
                break;
        }
        displayKMmode();
        displayPrevKM();
    }

    private void displayKMmode() {
        if (kmMode == KilometresMode.ODO)
            inputKM.setHint(getString(R.string.total_meter));
        else
            inputKM.setHint(getString(R.string.trip_meter));
    }

    private void displayPrevKM() {
        DriveObject driveObject = dbHelper.getPrevDrive(vehicleID);
        if (driveObject == null)
            prevKM.setText(String.format("odo: %dkm", vo.getInitKM()));
        else
            prevKM.setText(String.format("odo: %dkm", driveObject.getOdo()));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        changeKMmode(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        kmMode = KilometresMode.ODO;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(calendar.getTime());
        inputDate.getEditText().setText(date);
    }
}

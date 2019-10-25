package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.example.fueldiet.R;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewDriveActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private enum KilometresMode {
        ODO("Odo kilometers"), TRIP("Trip meter");

        public final String label;

        private KilometresMode(String label) {
            this.label = label;
        }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

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
                double total = Double.parseDouble(s.toString());
                double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                inputLPrice.getEditText().setText(String.valueOf(Utils.calculateLitrePrice(total, litres)));
                inputLPrice.getEditText().addTextChangedListener(litreprice);
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
                double lprice = Double.parseDouble(s.toString());
                double litres = Double.parseDouble(inputL.getEditText().getText().toString());
                inputPricePaid.getEditText().setText(String.valueOf(Utils.calculateFullPrice(lprice, litres)));
                inputPricePaid.getEditText().addTextChangedListener(fullprice);
            }
        };

        inputPricePaid.getEditText().addTextChangedListener(fullprice);

        inputLPrice.getEditText().addTextChangedListener(litreprice);


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
        String displayDate = inputDate.getEditText().getText().toString();
        String displayTime = inputTime.getEditText().getText().toString();
        int displayKm = Integer.parseInt(inputKM.getEditText().getText().toString());
        double displayLitre = Double.parseDouble(inputL.getEditText().getText().toString());
        double displayLitreEuro = Double.parseDouble(inputLPrice.getEditText().getText().toString());

        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        Cursor cursor = dbHelper.getPrevDrive(vehicleID);
        int prevOdo = cursor.getInt(0);
        Calendar preCal = Calendar.getInstance();
        preCal.setTimeInMillis(cursor.getLong(2)*1000);
        if (kmMode == KilometresMode.ODO) {
            if (prevOdo > displayKm) {
                Toast.makeText(this, "Total kilometers value is smaller than prev.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (c.getTimeInMillis() < preCal.getTimeInMillis()) {
                Toast.makeText(this, "Total kilometers are bigger than prev, yet time is before prev.", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.addDrive(vehicleID, displayLitre, displayLitreEuro, displayKm, displayKm-prevOdo, (c.getTimeInMillis()/1000));
        } else {
            if (c.getTimeInMillis() < preCal.getTimeInMillis()) {
                Toast.makeText(this, "Time is before prev.", Toast.LENGTH_SHORT).show();
                return;
            }
            dbHelper.addDrive(vehicleID, displayLitre, displayLitreEuro, prevOdo + displayKm, displayKm, (c.getTimeInMillis()/1000));
        }
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
        inputKM.setHint(kmMode.label);
    }

    private void displayPrevKM() {
        Cursor c = dbHelper.getPrevDrive(vehicleID);
        if (kmMode == KilometresMode.ODO) {
            prevKM.setText(String.format("%dkm", c.getInt(0)));
        } else {
            prevKM.setText(String.format("%dkm", c.getInt(1)));
        }
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
        inputTime.getEditText().setText(String.format("%d:%d", hourOfDay, minute));
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

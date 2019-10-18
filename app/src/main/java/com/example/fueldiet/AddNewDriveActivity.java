package com.example.fueldiet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewDriveActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private EditText inputDate;
    private EditText inputTime;

    private EditText inputKM;
    private EditText inputL;
    private EditText inputLPrice;
    private Spinner selectKM;

    private String kmMode;

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_drive);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        setVariables();

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.km_types, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectKM.setAdapter(adapterS);
        selectKM.setSelection(0);
        selectKM.setOnItemSelectedListener(this);

        inputTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });
        inputDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });


        FloatingActionButton addVehicle = findViewById(R.id.save_drive);
        addVehicle.setOnClickListener(v -> addNewDrive());
    }

    private void setVariables() {
        inputDate = findViewById(R.id.edit_date);
        inputTime = findViewById(R.id.edit_time);

        Calendar calendar = Calendar.getInstance();
        inputTime.setText(sdfTime.format(calendar.getTime()));
        inputDate.setText(sdfDate.format(calendar.getTime()));

        inputKM = findViewById(R.id.input_km);
        inputL = findViewById(R.id.input_l);
        inputLPrice = findViewById(R.id.input_litre_price);
        selectKM = findViewById(R.id.select_km_mode);
    }

    private void addNewDrive() {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        kmMode = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        kmMode = null;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        inputTime.setText(hourOfDay+":"+minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(calendar.getTime());
        inputDate.setText(date);
    }
}

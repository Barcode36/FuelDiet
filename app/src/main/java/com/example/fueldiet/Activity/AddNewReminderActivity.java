package com.example.fueldiet.Activity;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewReminderActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    enum ReminderMode {
        KM, TIME
    }

    private long vehicleID;
    FuelDietDBHelper dbHelper;
    private TextInputLayout inputDate;
    private TextInputLayout inputTime;
    private TextInputLayout inputKM;
    private TextInputLayout inputTitle;
    private TextInputLayout inputDesc;
    private Spinner inputTypeSpinner;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private ConstraintLayout mainKilometres;
    private TextView nowKM;
    private ConstraintLayout mainDate;
    private ConstraintLayout mainTime;

    private ReminderMode selectedMode;
    private Calendar hidCalendar;

    /*
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reminder);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_reminder_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        hidCalendar = Calendar.getInstance();

        setVariables();

        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.setArguments(currentDate);
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });


        FloatingActionButton addVehicle = findViewById(R.id.add_reminder_save);
        addVehicle.setOnClickListener(v -> addNewReminder());
    }

    private void setVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);
        inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.reminder_modes, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputTypeSpinner.setAdapter(adapterS);
        inputTypeSpinner.setOnItemSelectedListener(this);
        inputTypeSpinner.setSelection(0);

        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);

        mainDate = findViewById(R.id.add_reminder_date_constraint);
        mainTime = findViewById(R.id.add_reminder_time_constraint);
        mainKilometres = findViewById(R.id.add_reminder_km_constraint);
        nowKM = findViewById(R.id.add_reminder_now_km);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hidCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        hidCalendar.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        hidCalendar.set(Calendar.YEAR, year);
        hidCalendar.set(Calendar.MONTH, month);
        hidCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(hidCalendar.getTime());
        inputDate.getEditText().setText(date);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            selectedMode = ReminderMode.KM;
            hideAndShow();
        } else {
            selectedMode = ReminderMode.TIME;
            hideAndShow();
        }

    }

    private void hideAndShow() {
        if (selectedMode == ReminderMode.KM) {
            DriveObject drive = dbHelper.getLastDrive(vehicleID);
            mainKilometres.setVisibility(View.VISIBLE);
            mainDate.setVisibility(View.INVISIBLE);
            mainTime.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.VISIBLE);
            if (drive == null)
                nowKM.setText(String.format("odo km: no km yet"));
            else
                nowKM.setText(String.format("odo km: %d", drive.getOdo()));
        } else {
            mainKilometres.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.INVISIBLE);
            mainDate.setVisibility(View.VISIBLE);
            mainTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    public void addNewReminder() {
        String displayDate = inputDate.getEditText().getText().toString();
        String displayTime = inputTime.getEditText().getText().toString();
        int displayKm = 0;
        Calendar c = Calendar.getInstance();
        switch (selectedMode) {
            case KM:
                if (inputKM.getEditText().getText().toString().equals("")){
                    Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
                    return;
                }
                displayKm = Integer.parseInt(inputKM.getEditText().getText().toString());
                break;
            case TIME:
                String [] date = displayDate.split("\\.");
                String [] time = displayTime.split(":");
                c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                c.set(Calendar.SECOND, 0);
                break;
        }


        String displayTitle = inputTitle.getEditText().getText().toString();
        if (inputTitle.getEditText().getText().toString().equals("")){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString();
        if (displayDesc.equals(""))
            displayDesc = null;

        int id;
        switch (selectedMode) {
            case TIME:
                id = dbHelper.addReminder(vehicleID, displayTitle, (c.getTimeInMillis()/1000), displayDesc);
                Utils.startAlarm(c, id, this, vehicleID);
                break;
            case KM:
                dbHelper.addReminder(vehicleID, displayTitle, displayKm, displayDesc);
                //Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
                break;
        }

        Intent intent = new Intent(AddNewReminderActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        intent.putExtra("frag", 2);
        //startActivity(intent);
        finish();
    }
}

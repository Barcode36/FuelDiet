package com.fueldiet.fueldiet.activity;

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

import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
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
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private ConstraintLayout mainKilometres;
    private TextView nowKM;
    private ConstraintLayout mainDate;
    private ConstraintLayout mainTime;

    private ReminderMode selectedMode;
    private Calendar hidCalendar;


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

        initVariables();

        /* Open time/date dialog */
        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.setArguments(currentDate);
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        /* Open time/date dialog */
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

        /* save reminder */
        FloatingActionButton addVehicle = findViewById(R.id.add_reminder_save);
        addVehicle.setOnClickListener(v -> addNewReminder());
    }

    /**
     * Connect fields with variables
     */
    private void initVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);
        Spinner inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

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

    /**
     * @param parent parent
     * @param view view
     * @param position selected mode
     * @param id id
     */
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

    /**
     * Either hides km field or date&time
     */
    private void hideAndShow() {
        if (selectedMode == ReminderMode.KM) {
            CostObject cost = dbHelper.getPrevCost(vehicleID);
            ReminderObject reminder = dbHelper.getBiggestReminder(vehicleID);
            mainKilometres.setVisibility(View.VISIBLE);
            mainDate.setVisibility(View.INVISIBLE);
            mainTime.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.VISIBLE);

            int max = dbHelper.getVehicle(vehicleID).getOdoKm();
            int costKm = cost.getKm();
            int remKm = reminder.getKm();
            if (cost.getResetKm() == 1) {
                costKm = 0;
                remKm = 0;
            }

            int tmp = Math.max(max > costKm ? max : costKm, remKm);

            max = tmp;
            if (max != 0)
                nowKM.setText(String.format("odo km: %d", max));
            else
                nowKM.setText(R.string.odo_km_no_km_yet);
        } else {
            mainKilometres.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.INVISIBLE);
            mainDate.setVisibility(View.VISIBLE);
            mainTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /**
     * Save new reminder
     */
    public void addNewReminder() {
        int displayKm = 0;
        if (selectedMode == ReminderMode.KM) {
            if (inputKM.getEditText().getText().toString().trim().equals("")){
                Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
                return;
            }
            displayKm = Integer.parseInt(inputKM.getEditText().getText().toString().trim());
        }

        String displayTitle = inputTitle.getEditText().getText().toString().trim();
        if (displayTitle.equals("")){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString().trim();
        if (displayDesc.equals(""))
            displayDesc = null;

        int id;
        switch (selectedMode) {
            case TIME:
                id = dbHelper.addReminder(vehicleID, displayTitle, (hidCalendar.getTimeInMillis()/1000), displayDesc);
                Utils.startAlarm(hidCalendar, id, this, vehicleID);
                break;
            case KM:
                dbHelper.addReminder(vehicleID, displayTitle, displayKm, displayDesc);
                break;
        }
        finish();
    }
}

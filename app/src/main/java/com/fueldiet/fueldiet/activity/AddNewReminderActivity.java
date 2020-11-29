package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.TimeDatePickerHelper;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class AddNewReminderActivity extends BaseActivity {

    private static String TAG = "AddNewReminderActivity";

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
    private TextInputLayout inputEvery;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private SwitchMaterial switchRepeat;

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
        dbHelper = FuelDietDBHelper.getInstance(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        hidCalendar = Calendar.getInstance();
        hidCalendar.add(Calendar.MINUTE, 2);
        hidCalendar.set(Calendar.SECOND, 0);
        hidCalendar.set(Calendar.MILLISECOND, 0);

        initVariables();

        /* Open time/date dialog */
        inputTime.getEditText().setOnClickListener(v -> {
            MaterialTimePicker materialTimePicker = TimeDatePickerHelper.createTime(hidCalendar);
            materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "on time change: " + materialTimePicker.getHour() + ":" + materialTimePicker.getMinute());
                    hidCalendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
                    hidCalendar.set(Calendar.MINUTE, materialTimePicker.getMinute());
                    inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
                }
            });
        });

        /* Open time/date dialog */
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

        switchRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                inputEvery.setVisibility(View.VISIBLE);
            else
                inputEvery.setVisibility(View.GONE);
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
        MaterialButtonToggleGroup inputTypeToggle = findViewById(R.id.add_reminder_mode_toggle);

        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(hidCalendar.getTime()));
        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);
        switchRepeat = findViewById(R.id.add_reminder_repeat);
        inputEvery = findViewById(R.id.add_reminder_every_input);

        inputTypeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                Log.d(TAG, "onButtonChecked: "+group+", "+checkedId+", "+isChecked);
                if (checkedId == R.id.add_reminder_mode_time) {
                    selectedMode = ReminderMode.TIME;
                } else {
                    selectedMode = ReminderMode.KM;
                }
                hideAndShow();
            }
        });
        inputTypeToggle.check(R.id.add_reminder_mode_time);
    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
    }

    /**
     * Either hides km field or date&time
     */
    private void hideAndShow() {
        if (selectedMode == ReminderMode.KM) {
            Log.d(TAG, "hideAndShow: selected km");
            VehicleObject vehicleObject = dbHelper.getVehicle(vehicleID);

            inputKM.setVisibility(View.VISIBLE);
            inputDate.setVisibility(View.INVISIBLE);
            inputTime.setVisibility(View.INVISIBLE);

            inputEvery.setHint(getString(R.string.repeat_every_x) + " km");

            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                inputKM.setHelperText(String.format("ODO: %d", max));
            else
                inputKM.setHelperText(getString(R.string.odo_km_no_km_yet));
        } else {
            Log.d(TAG, "hideAndShow: selected time");
            inputKM.setVisibility(View.INVISIBLE);
            inputDate.setVisibility(View.VISIBLE);
            inputTime.setVisibility(View.VISIBLE);
            inputEvery.setHint(getString(R.string.repeat_every_x) + " days");
        }
    }

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
            VehicleObject vehicleObject = dbHelper.getVehicle(vehicleID);
            int odo = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            odo = Math.max(odo, vehicleObject.getOdoRemindKm());
            if (displayKm < odo) {
                Toast.makeText(this, getString(R.string.km_is_smaller_than_prev), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (hidCalendar.before(Calendar.getInstance())) {
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String displayTitle = inputTitle.getEditText().getText().toString().trim();
        if (displayTitle.equals("")){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString().trim();
        if (displayDesc.equals(""))
            displayDesc = null;


        int rpt = 0;
        if (switchRepeat.isChecked()) {
            int every = Integer.parseInt(inputEvery.getEditText().getText().toString());
            rpt = every;
            if (displayDesc == null)
                displayDesc = "0//-";
            else
                displayDesc = "0//-" + displayDesc;
        }

        int id;
        switch (selectedMode) {
            case TIME:
                id = dbHelper.addReminder(vehicleID, displayTitle, (hidCalendar.getTimeInMillis()/1000), displayDesc, rpt);
                Utils.startAlarm(hidCalendar, id, getApplicationContext(), vehicleID);
                break;
            case KM:
                dbHelper.addReminder(vehicleID, displayTitle, displayKm, displayDesc, rpt);
                break;
        }
        finish();
    }
}

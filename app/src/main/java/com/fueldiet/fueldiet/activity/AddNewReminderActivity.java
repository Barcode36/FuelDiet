package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.utils.TextInputValidator;
import com.fueldiet.fueldiet.utils.TimeDatePickerHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AddNewReminderActivity extends BaseActivity {

    private static final String TAG = "AddNewReminderActivity";

    enum ReminderMode {
        KM, TIME
    }

    private long vehicleID;
    FuelDietDBHelper dbHelper;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;
    private TextInputLayout inputKm;
    private TextInputLayout inputTitle;
    private TextInputLayout inputDesc;
    private TextInputLayout inputEvery;

    private TextInputEditText inputTitleEdit;
    private TextInputEditText inputEveryEdit;
    private TextInputEditText inputKmEdit;

    private TextInputValidator validatorKm;
    private TextInputValidator validatorEvery;
    private TextInputValidator validatorTitle;

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private SwitchMaterial switchRepeat;

    private ReminderMode selectedMode;
    private Calendar hidCalendar;

    Locale locale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reminder);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_reminder_title);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        hidCalendar = Calendar.getInstance();
        hidCalendar.add(Calendar.MINUTE, 2);
        hidCalendar.set(Calendar.SECOND, 0);
        hidCalendar.set(Calendar.MILLISECOND, 0);

        initVariables();
        addValidators();

        /* Open time/date dialog */
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
        inputKm = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);
        switchRepeat = findViewById(R.id.add_reminder_repeat);
        inputEvery = findViewById(R.id.add_reminder_every_input);

        this.inputKmEdit = findViewById(R.id.add_reminder_km_input_edit);
        this.inputTitleEdit = findViewById(R.id.add_reminder_title_input_edit);
        this.inputEveryEdit = findViewById(R.id.add_reminder_every_input_edit);

        inputTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.d(TAG, "onButtonChecked: "+group+", "+checkedId+", "+isChecked);
            if (checkedId == R.id.add_reminder_mode_time) {
                selectedMode = ReminderMode.TIME;
            } else {
                selectedMode = ReminderMode.KM;
            }
            hideAndShow();
        });
        inputTypeToggle.check(R.id.add_reminder_mode_time);
    }

    private void addValidators() {
        this.validatorKm = new TextInputValidator(this, locale, this.inputKm, this.inputKmEdit);
        this.validatorTitle = new TextInputValidator(this, locale, this.inputTitle, this.inputTitleEdit);
        this.validatorEvery = new TextInputValidator(this, locale, this.inputEvery, this.inputEveryEdit);
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

            inputKm.setVisibility(View.VISIBLE);
            inputDate.setVisibility(View.INVISIBLE);
            inputTime.setVisibility(View.INVISIBLE);

            inputEvery.setHint(getString(R.string.repeat_every_x) + " km");

            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                inputKm.setHelperText(String.format(locale, "ODO: %d", max));
            else
                inputKm.setHelperText(getString(R.string.odo_km_no_km_yet));
        } else {
            Log.d(TAG, "hideAndShow: selected time");
            inputKm.setVisibility(View.INVISIBLE);
            inputDate.setVisibility(View.VISIBLE);
            inputTime.setVisibility(View.VISIBLE);
            inputEvery.setHint(getString(R.string.repeat_every_x) + " days");
        }
    }

    /**
     * Save new reminder
     */
    public void addNewReminder() {

        boolean titleStatus = this.validatorTitle.isEmpty();
        if (titleStatus) {
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }

        ReminderObject ro = new ReminderObject();
        ro.setCarID(vehicleID);
        ro.setTitle(Objects.requireNonNull(inputTitle.getEditText()).getText().toString().trim());
        String displayDesc = Objects.requireNonNull(inputDesc.getEditText()).getText().toString().trim();
        if (displayDesc.equals("")) {
            displayDesc = null;
        }

        if (switchRepeat.isChecked()) {
            if (this.validatorEvery.isEmpty()) {
                return;
            }
            ro.setRepeat(Integer.parseInt(Objects.requireNonNull(inputEvery.getEditText()).getText().toString()));
            if (displayDesc == null) {
                displayDesc = "0//-";
            } else {
                displayDesc = "0//-" + displayDesc;
            }
        }
        ro.setDesc(displayDesc);

        if (selectedMode == ReminderMode.KM) {
            VehicleObject vehicleObject = dbHelper.getVehicle(vehicleID);
            int odo = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            odo = Math.max(odo, vehicleObject.getOdoRemindKm());

            if (this.validatorKm.isEmpty() || this.validatorKm.areKilometresWrong(getString(R.string.total_meter), odo)){
                Toast.makeText(this, getString(R.string.wrong_km), Toast.LENGTH_SHORT).show();
                return;
            }
            ro.setKm(Objects.requireNonNull(inputKm.getEditText()).getText().toString().trim());

            dbHelper.addReminder(ro);
        } else {
            if (hidCalendar.before(Calendar.getInstance())) {
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            }
            ro.setDate(hidCalendar.getTime());
            long id = dbHelper.addReminder(ro);
            Utils.startAlarm(hidCalendar, (int) id, getApplicationContext(), vehicleID);
        }
        finish();
    }
}

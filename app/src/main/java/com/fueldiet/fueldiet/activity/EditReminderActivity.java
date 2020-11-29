package com.fueldiet.fueldiet.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.fueldiet.fueldiet.AlertReceiver;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.TimeDatePickerHelper;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class EditReminderActivity extends BaseActivity {
    private static final String TAG = "EditReminderActivity";
    private FuelDietDBHelper dbHelper;
    private int reminderID;
    private ReminderObject reminderObject;
    private long vehicleID;
    private VehicleObject vehicleObject;
    private TextInputLayout inputDate;
    private TextInputLayout inputTime;
    private TextInputLayout inputKM;
    private TextInputLayout inputTitle;
    private TextInputLayout inputDesc;
    private TextInputLayout inputEvery;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;
    private SwitchMaterial switchRepeat;
    private AddNewReminderActivity.ReminderMode selectedMode;
    private Locale locale;
    private String repeated;
    private Calendar hidCalendar;
    private boolean repeatReminder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reminder);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_reminder_title);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", 1L);
        reminderID = intent.getIntExtra("reminder_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        vehicleObject = dbHelper.getVehicle(vehicleID);
        reminderObject = dbHelper.getReminder(reminderID);
        hidCalendar = Calendar.getInstance();

        initVariables();
        fillVariables();

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

        /* save reminder */
        FloatingActionButton addVehicle = findViewById(R.id.add_reminder_save);
        addVehicle.setOnClickListener(v -> saveReminder());
    }

    /**
     * Connect fields with variables
     */
    private void initVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);

        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);

        switchRepeat = findViewById(R.id.add_reminder_repeat);
        inputEvery = findViewById(R.id.add_reminder_every_input);

        MaterialButtonToggleGroup inputTypeToggle = findViewById(R.id.add_reminder_mode_toggle);
        inputTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.add_reminder_mode_time) {
                selectedMode = AddNewReminderActivity.ReminderMode.KM;
            } else {
                selectedMode = AddNewReminderActivity.ReminderMode.TIME;
            }
            hideAndShow();
        });

        inputTypeToggle.setEnabled(false);
    }

    @SuppressWarnings("ConstantConditions")
    private void fillVariables() {
        MaterialButtonToggleGroup inputTypeToggle = findViewById(R.id.add_reminder_mode_toggle);
        if (reminderObject.getRepeat() != 0) {
            switchRepeat.setChecked(true);
            repeatReminder = true;
        } else {
            repeatReminder = false;
        }
        switchRepeat.setEnabled(false);

        inputTitle.getEditText().setText(reminderObject.getTitle());
        if (reminderObject.getKm() == null && reminderObject.getDate() != null) {
            inputTypeToggle.check(R.id.add_reminder_mode_time);
            selectedMode = AddNewReminderActivity.ReminderMode.TIME;
            hidCalendar.setTime(reminderObject.getDate());
            inputTime.getEditText().setText(sdfTime.format(reminderObject.getDate()));
            inputDate.getEditText().setText(sdfDate.format(reminderObject.getDate()));
        } else if (reminderObject.getKm() != null && reminderObject.getDate() == null) {
            inputTypeToggle.check(R.id.add_reminder_mode_dist);
            selectedMode = AddNewReminderActivity.ReminderMode.KM;
            inputKM.getEditText().setText(String.format(locale, "%d", reminderObject.getKm()));
        } else {
            selectedMode = null;
            //finished reminder
            inputTime.getEditText().setText(sdfTime.format(reminderObject.getDate()));
            inputDate.getEditText().setText(sdfDate.format(reminderObject.getDate()));
            inputKM.getEditText().setText(String.format(locale, "%d", reminderObject.getKm()));
        }
        if (repeatReminder) {
            //rpt
            String [] desc = reminderObject.getDesc().split("//-");
            if (desc.length == 2)
                inputDesc.getEditText().setText(desc[1]);
            repeated = desc[0];
            inputEvery.setVisibility(View.VISIBLE);
            inputEvery.getEditText().setText(String.format(locale, "%d", reminderObject.getRepeat()));
        } else {
            inputDesc.getEditText().setText(reminderObject.getDesc());
        }
        hideAndShow();
    }

    /**
     * Either hides km field or date&time
     */
    private void hideAndShow() {
        if (selectedMode == AddNewReminderActivity.ReminderMode.KM) {
            VehicleObject vehicleObject = dbHelper.getVehicle(vehicleID);

            inputKM.setVisibility(View.VISIBLE);
            inputDate.setVisibility(View.INVISIBLE);
            inputTime.setVisibility(View.INVISIBLE);

            inputEvery.setHint(getString(R.string.repeat_every_x) + " km");

            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                inputKM.setHelperText(String.format(locale, "ODO: %d", max));
            else
                inputKM.setHelperText(getString(R.string.odo_km_no_km_yet));
        } else if (selectedMode == null) {
            //finished
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone((ConstraintLayout) findViewById(R.id.add_reminder_constraint_layout_inner));
            constraintSet.connect(R.id.add_reminder_km_input, ConstraintSet.TOP, R.id.add_reminder_date_input, ConstraintSet.BOTTOM,10);
            constraintSet.applyTo((ConstraintLayout) findViewById(R.id.add_reminder_constraint_layout_inner));

            findViewById(R.id.add_reminder_mode_toggle).setVisibility(View.GONE);
            switchRepeat.setVisibility(View.GONE);
            findViewById(R.id.add_reminder_when).setVisibility(View.GONE);
            findViewById(R.id.add_reminder_first_break).setVisibility(View.GONE);
            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                inputKM.setHelperText(String.format(locale, "ODO: %d", max));
            else
                inputKM.setHelperText(getString(R.string.odo_km_no_km_yet));

        } else {
            inputKM.setVisibility(View.INVISIBLE);
            inputDate.setVisibility(View.VISIBLE);
            inputTime.setVisibility(View.VISIBLE);
            inputEvery.setHint(getString(R.string.repeat_every_x) + " days");
        }
    }

    private void saveReminder() {
        //TODO: delete old alert if exists; already done?
        //TODO: finished reminders

        String displayTitle = inputTitle.getEditText().getText().toString().trim();
        if (displayTitle.equals("")){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString().trim();
        if (displayDesc.equals(""))
            displayDesc = null;
        if (repeatReminder) {
            if (displayDesc == null)
                displayDesc = repeated.concat("//-");
            else
                displayDesc = repeated + "//-" + displayDesc;
        }

        reminderObject.setTitle(displayTitle);
        reminderObject.setDesc(displayDesc);

        if (selectedMode == AddNewReminderActivity.ReminderMode.KM) {
            int displayKm, repeatKm;
            if (inputKM.getEditText().getText().toString().trim().equals("")){
                Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
                return;
            }
            displayKm = Integer.parseInt(inputKM.getEditText().getText().toString().trim());
            reminderObject.setKm(displayKm);
            if (repeatReminder) {
                if (inputEvery.getEditText().getText().toString().trim().equals("")) {
                    Toast.makeText(this, getString(R.string.insert_rpt_interval), Toast.LENGTH_SHORT).show();
                    return;
                }
                reminderObject.setRepeat(Integer.parseInt(inputEvery.getEditText().getText().toString()));
                repeatKm = Integer.parseInt(inputEvery.getEditText().getText().toString());
            } else {
                repeatKm = 0;
            }
            //check that new km is bigger than biggest odo
            //for rpt check that rpt num * km > biggest odo

            int odo = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            odo = Math.max(odo, vehicleObject.getOdoRemindKm());
            if (repeatReminder) {
                if ((displayKm) + repeatKm + repeatKm * Integer.parseInt(repeated) < odo) {
                    Toast.makeText(this, getString(R.string.km_is_smaller_than_prev), Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.updateReminder(reminderObject);
            } else {
                if (displayKm < odo) {
                    Toast.makeText(this, getString(R.string.km_is_smaller_than_prev), Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.updateReminder(reminderObject);
            }
        } else if (selectedMode == AddNewReminderActivity.ReminderMode.TIME) {
            reminderObject.setDate(hidCalendar.getTime());
            int repeatInterval = 0;
            if (repeatReminder) {
                if (inputEvery.getEditText().getText().toString().trim().equals("")) {
                    Toast.makeText(this, getString(R.string.insert_rpt_interval), Toast.LENGTH_SHORT).show();
                    return;
                }
                reminderObject.setRepeat(Integer.parseInt(inputEvery.getEditText().getText().toString()));
                repeatInterval = Integer.parseInt(inputEvery.getEditText().getText().toString());
            }

            //check that time is after current
            //multiply it for repeated
            //delete and create new alert

            Calendar now = hidCalendar;
            if (repeatReminder) {
                now.add(Calendar.DAY_OF_MONTH, repeatInterval + (repeatInterval * Integer.parseInt(repeated)));
            }
            if (hidCalendar.before(Calendar.getInstance())) {
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            }

            //alerts
            AlarmManager alarmManager = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getBaseContext(), AlertReceiver.class);
            intent.putExtra("vehicle_id", reminderObject.getCarID());
            intent.putExtra("reminder_id", reminderObject.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), reminderObject.getId(), intent, 0);
            try {
                alarmManager.cancel(pendingIntent);
            } catch (NullPointerException e) {
                Log.e(TAG, "saveReminder: alertManager is null:", e.fillInStackTrace());
            }

            Utils.startAlarm(now, reminderObject.getId(), getApplicationContext(), vehicleID);
            dbHelper.updateReminder(reminderObject);
            finish();

        } else {
            //finished
            //TODO check if latest and update vehicle rem odo
            int displayKm;
            if (inputKM.getEditText().getText().toString().trim().equals("")){
                Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
                return;
            }
            displayKm = Integer.parseInt(inputKM.getEditText().getText().toString().trim());
            reminderObject.setKm(displayKm);
            reminderObject.setDate(hidCalendar.getTime());
        }
    }
}

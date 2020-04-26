package com.fueldiet.fueldiet.activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;

import com.fueldiet.fueldiet.AlertReceiver;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditReminderActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
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
    private Switch switchRepeat;
    private ConstraintLayout mainKilometres;
    private TextView nowKM;
    private ConstraintLayout mainDate;
    private ConstraintLayout mainTime;
    private ConstraintLayout mainEvery;
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
        dbHelper = new FuelDietDBHelper(this);

        vehicleObject = dbHelper.getVehicle(vehicleID);
        reminderObject = dbHelper.getReminder(reminderID);
        hidCalendar = Calendar.getInstance();

        initVariables();
        fillVariables();

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
        addVehicle.setOnClickListener(v -> saveReminder());
    }

    /**
     * Connect fields with variables
     */
    private void initVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);
        Spinner inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.reminder_modes, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputTypeSpinner.setAdapter(adapterS);

        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);

        mainDate = findViewById(R.id.add_reminder_date_constraint);
        mainTime = findViewById(R.id.add_reminder_time_constraint);
        mainKilometres = findViewById(R.id.add_reminder_km_constraint);
        nowKM = findViewById(R.id.add_reminder_now_km);

        switchRepeat = findViewById(R.id.add_reminder_repeat);
        inputEvery = findViewById(R.id.add_reminder_every_input);
        mainEvery = findViewById(R.id.add_reminder_every_constraint);

        inputTypeSpinner.setEnabled(false);
    }

    @SuppressWarnings("ConstantConditions")
    private void fillVariables() {
        Spinner inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

        if (reminderObject.getRepeat() != 0) {
            switchRepeat.setChecked(true);
            repeatReminder = true;
        } else {
            repeatReminder = false;
        }
        switchRepeat.setEnabled(false);

        inputTitle.getEditText().setText(reminderObject.getTitle());
        if (reminderObject.getKm() == null && reminderObject.getDate() != null) {
            inputTypeSpinner.setSelection(1);
            selectedMode = AddNewReminderActivity.ReminderMode.TIME;
            hidCalendar.setTime(reminderObject.getDate());
            inputTime.getEditText().setText(sdfTime.format(reminderObject.getDate()));
            inputDate.getEditText().setText(sdfDate.format(reminderObject.getDate()));
        } else if (reminderObject.getKm() != null && reminderObject.getDate() == null) {
            inputTypeSpinner.setSelection(0);
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
            mainEvery.setVisibility(View.VISIBLE);
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

            mainKilometres.setVisibility(View.VISIBLE);
            mainDate.setVisibility(View.INVISIBLE);
            mainTime.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.VISIBLE);

            inputEvery.setHint(getString(R.string.repeat_every_x) + " km");

            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                nowKM.setText(String.format(locale, "ODO: %d", max));
            else
                nowKM.setText(R.string.odo_km_no_km_yet);
        } else if (selectedMode == null) {
            //finished
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone((ConstraintLayout) findViewById(R.id.add_reminder_constraint_layout_inner));
            constraintSet.connect(R.id.add_reminder_km_constraint, ConstraintSet.TOP, R.id.add_reminder_date_constraint, ConstraintSet.BOTTOM,10);
            constraintSet.applyTo((ConstraintLayout) findViewById(R.id.add_reminder_constraint_layout_inner));

            findViewById(R.id.add_reminder_category_constraint).setVisibility(View.GONE);
            switchRepeat.setVisibility(View.GONE);
            findViewById(R.id.add_reminder_when).setVisibility(View.GONE);
            findViewById(R.id.add_reminder_first_break).setVisibility(View.GONE);
            int max = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            max = Math.max(max, vehicleObject.getOdoRemindKm());

            if (max != 0)
                nowKM.setText(String.format(locale, "ODO: %d", max));
            else
                nowKM.setText(R.string.odo_km_no_km_yet);

        } else {
            mainKilometres.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.INVISIBLE);
            mainDate.setVisibility(View.VISIBLE);
            mainTime.setVisibility(View.VISIBLE);
            inputEvery.setHint(getString(R.string.repeat_every_x) + " days");
        }
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

    private void saveReminder() {
        //TODO: delete old alert if exists
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
            int repeatInteval = 0;
            if (repeatReminder) {
                if (inputEvery.getEditText().getText().toString().trim().equals("")) {
                    Toast.makeText(this, getString(R.string.insert_rpt_interval), Toast.LENGTH_SHORT).show();
                    return;
                }
                reminderObject.setRepeat(Integer.parseInt(inputEvery.getEditText().getText().toString()));
                repeatInteval = Integer.parseInt(inputEvery.getEditText().getText().toString());
            }

            //check that time is after current
            //multiply it for repeated
            //delete and create new alert

            Calendar now = hidCalendar;
            if (repeatReminder) {
                now.add(Calendar.DAY_OF_MONTH, repeatInteval + repeatInteval * Integer.parseInt(repeated));
                if (hidCalendar.before(Calendar.getInstance())) {
                    Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (hidCalendar.before(Calendar.getInstance())) {
                    Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                    return;
                }
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

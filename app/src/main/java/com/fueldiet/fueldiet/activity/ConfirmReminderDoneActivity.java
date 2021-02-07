package com.fueldiet.fueldiet.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.utils.TimeDatePickerHelper;
import com.google.android.material.button.MaterialButton;
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

public class ConfirmReminderDoneActivity extends BaseActivity {

    private static String TAG = "ConfirmReminderDoneActivity";

    private ReminderObject reminder;
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
    private MaterialButton useLatestKm;
    private TextView when_to_remind;
    private MaterialButtonToggleGroup type;

    private Calendar hidCalendar;
    private Locale locale;
    private String rptNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reminder);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.confirm_reminder_done);

        Intent intent = getIntent();
        int reminderID = intent.getIntExtra("reminder_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);
        reminder = dbHelper.getReminder(reminderID);

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        hidCalendar = Calendar.getInstance();

        setVariables();
        fixFields();
        fillFields();

        useLatestKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VehicleObject vehicleObject = dbHelper.getVehicle(reminder.getCarID());
                int odo = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
                odo = Math.max(odo, vehicleObject.getOdoRemindKm());
                inputKM.getEditText().setText(String.format(locale, "%d",odo));
            }
        });

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

        /* confirm done */
        FloatingActionButton addVehicle = findViewById(R.id.add_reminder_save);
        addVehicle.setImageResource(R.drawable.ic_check_black_24dp);
        addVehicle.setOnClickListener(v -> doneReminder());
    }

    /**
     * Confirm reminder is done
     */
    private void doneReminder() {
        boolean ok = true;

        reminder.setDate(hidCalendar.getTime());
        ok = ok && reminder.setTitle(inputTitle.getEditText().getText().toString());
        if (!ok) {
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString().trim();
        if (displayDesc.equals(""))
            displayDesc = null;

        if (reminder.getRepeat() != 0) {
            if (displayDesc == null)
                displayDesc = rptNum.concat("//-");
            else
                displayDesc = rptNum.concat("//-").concat(displayDesc);
        }

        ok = ok && reminder.setDesc(displayDesc);
        ok = ok && reminder.setKm(inputKM.getEditText().getText().toString());
        if (!ok) {
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return;
        }

        // check for date and km conflicts
        ReminderObject prevRem = dbHelper.getPrevReminder(reminder);
        ReminderObject nextRem = dbHelper.getNextReminder(reminder);
        VehicleObject vehicleObject = dbHelper.getVehicle(reminder.getCarID());

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //StatusBarNotification[] current = manager.getActiveNotifications();

        if (prevRem == null && nextRem == null) {
            dbHelper.updateReminder(reminder);
            vehicleObject.setOdoRemindKm(reminder.getKm());
            dbHelper.updateVehicle(vehicleObject);
            manager.cancel(reminder.getId());
        } else if (nextRem == null) {
            //imamo zadnjega
            if (prevRem.getDate().before(reminder.getDate())) {
                dbHelper.updateReminder(reminder);
                vehicleObject.setOdoRemindKm(reminder.getKm());
                dbHelper.updateVehicle(vehicleObject);
                manager.cancel(reminder.getId());
            } else {
                //prejšnji po km ima večji datum
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (prevRem == null) {
            //prvi
            if (nextRem.getDate().after(reminder.getDate())) {
                dbHelper.updateReminder(reminder);
                vehicleObject.setOdoRemindKm(reminder.getKm());
                dbHelper.updateVehicle(vehicleObject);
                manager.cancel(reminder.getId());
            } else {
                //večji km a manjši datum
                Toast.makeText(this, getString(R.string.bigger_km_smaller_time), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            //sredina
            if (prevRem.getDate().before(reminder.getDate())) {
                if (nextRem.getDate().after(reminder.getDate())) {
                    //ok
                    dbHelper.updateReminder(reminder);
                    vehicleObject.setOdoRemindKm(reminder.getKm());
                    dbHelper.updateVehicle(vehicleObject);
                    manager.cancel(reminder.getId());
                } else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this, locale);
        automaticBackup.createBackup(this);
    }

    /**
     * Connect variables with fields
     */
    private void setVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);
        type = findViewById(R.id.add_reminder_mode_toggle);

        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));
        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);
        when_to_remind = findViewById(R.id.add_reminder_when);
        switchRepeat = findViewById(R.id.add_reminder_repeat);
        inputEvery = findViewById(R.id.add_reminder_every_input);
        useLatestKm = findViewById(R.id.add_reminder_use_latest_km);

    }

    /**
     * Fix layout
     */
    private void fixFields() {
        type.setVisibility(View.GONE);
        when_to_remind.setVisibility(View.GONE);
        switchRepeat.setVisibility(View.GONE);
        inputEvery.setVisibility(View.GONE);
        useLatestKm.setVisibility(View.VISIBLE);

        ConstraintLayout parent = findViewById(R.id.add_reminder_constraint_layout_inner);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parent);
        constraintSet.connect(inputKM.getId(), ConstraintSet.TOP, inputDate.getId(), ConstraintSet.BOTTOM,10);
        constraintSet.applyTo(parent);

        ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) inputKM.getLayoutParams();
        newLayoutParams.setMarginEnd(16);
        inputKM.setLayoutParams(newLayoutParams);
    }

    /**
     * Fill fields with reminder data
     */
    private void fillFields() {
        inputKM.setHint("Reminded at");
        if (reminder.getKm() == null) {
            //no km
            inputDate.getEditText().setText(sdfDate.format(reminder.getDate()));
            inputTime.getEditText().setText(sdfTime.format(reminder.getDate()));
        } else if (reminder.getDate() == null){
            //no date
            inputKM.getEditText().setText(String.format(locale, "%d", reminder.getKm()));
        } else {
            //editing done reminder
            inputKM.getEditText().setText(String.format(locale, "%d", reminder.getKm()));
            inputDate.getEditText().setText(sdfDate.format(reminder.getDate()));
            inputTime.getEditText().setText(sdfTime.format(reminder.getDate()));
        }
        inputTitle.getEditText().setText(reminder.getTitle());

        if (reminder.getRepeat() != 0) {
            String [] desc = reminder.getDesc().split("//-");
            if (desc.length == 2)
                inputDesc.getEditText().setText(desc[1]);
            rptNum = desc[0];
        } else {
            inputDesc.getEditText().setText(reminder.getDesc());
            rptNum = null;
        }
    }
}

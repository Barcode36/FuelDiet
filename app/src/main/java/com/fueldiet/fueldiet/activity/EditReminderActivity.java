package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
    private Switch switchRepeat;
    private ConstraintLayout mainKilometres;
    private TextView nowKM;
    private ConstraintLayout mainDate;
    private ConstraintLayout mainTime;
    private ConstraintLayout mainEvery;
    private AddNewReminderActivity.ReminderMode selectedMode;
    private Calendar hidCalendar;
    private Locale locale;

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

        initVariables();
        fillVariables();
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

    private void fillVariables() {
        Spinner inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

        if (reminderObject.getRepeat() != 0) {
            switchRepeat.setChecked(true);
        }
        switchRepeat.setEnabled(false);

        inputTitle.getEditText().setText(reminderObject.getTitle());
        if (reminderObject.getKm() == null && reminderObject.getDate() != null) {
            inputTypeSpinner.setSelection(1);
            selectedMode = AddNewReminderActivity.ReminderMode.TIME;
            inputTime.getEditText().setText(sdfTime.format(reminderObject.getDate()));
            inputDate.getEditText().setText(sdfDate.format(reminderObject.getDate()));
        } else if (reminderObject.getKm() != null && reminderObject.getDate() == null) {
            inputTypeSpinner.setSelection(0);
            selectedMode = AddNewReminderActivity.ReminderMode.KM;
            inputKM.getEditText().setText(String.format(locale, "%d", reminderObject.getKm()));
        } else {
            selectedMode = null;
            //finished reminder
        }
        if (switchRepeat.isChecked()) {
            //rpt
            String [] desc = reminderObject.getDesc().split("//-");
            inputDesc.getEditText().setText(desc[1]);
            mainEvery.setVisibility(View.VISIBLE);
            nowKM.setText(String.format(locale, "%s", reminderObject.getRepeat()));
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
        } else {
            mainKilometres.setVisibility(View.INVISIBLE);
            nowKM.setVisibility(View.INVISIBLE);
            mainDate.setVisibility(View.VISIBLE);
            mainTime.setVisibility(View.VISIBLE);
            inputEvery.setHint(getString(R.string.repeat_every_x) + " days");
        }
    }
}

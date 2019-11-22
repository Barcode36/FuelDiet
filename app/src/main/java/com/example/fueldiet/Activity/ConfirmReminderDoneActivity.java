package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.DialogFragment;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ConfirmReminderDoneActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private int reminderID;
    private ReminderObject reminder;
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
    private TextView when_to_remind;
    private ConstraintLayout type;

    private Calendar hidCalendar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_reminder);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.confirm_reminder_done);

        Intent intent = getIntent();
        reminderID = intent.getIntExtra("reminder_id", 1);
        dbHelper = new FuelDietDBHelper(this);
        reminder = dbHelper.getReminder(reminderID);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        hidCalendar = Calendar.getInstance();

        setVariables();
        fixFields();
        fillFields();

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
        ok = ok && reminder.setDesc(inputDesc.getEditText().getText().toString());
        ok = ok && reminder.setKm(inputKM.getEditText().getText().toString());
        if (!ok) {
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return;
        }

        // check for date and km conflicts
        ReminderObject prevRem = dbHelper.getPrevReminder(reminder);
        ReminderObject nextRem = dbHelper.getNextReminder(reminder);

        if (prevRem == null && nextRem == null) {
            dbHelper.updateReminder(reminder);
        } else if (nextRem == null) {
            //imamo zadnjega
            if (prevRem.getDate().before(reminder.getDate()))
                dbHelper.updateReminder(reminder);
            else {
                //prejšnji po km ima večji datum
                Toast.makeText(this, getString(R.string.km_ok_time_not), Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (prevRem == null) {
            //prvi
            if (nextRem.getDate().after(reminder.getDate()))
                dbHelper.updateReminder(reminder);
            else {
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

    /**
     * Connect variables with fields
     */
    private void setVariables() {
        inputDate = findViewById(R.id.add_reminder_date_input);
        inputTime = findViewById(R.id.add_reminder_time_input);
        type = findViewById(R.id.add_reminder_category_constraint);


        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));

        inputKM = findViewById(R.id.add_reminder_km_input);
        inputTitle = findViewById(R.id.add_reminder_title_input);
        inputDesc = findViewById(R.id.add_reminder_note_input);

        mainDate = findViewById(R.id.add_reminder_date_constraint);
        when_to_remind = findViewById(R.id.add_reminder_when);
        mainKilometres = findViewById(R.id.add_reminder_km_constraint);
        nowKM = findViewById(R.id.add_reminder_now_km);

    }

    /**
     * Fix layout
     */
    private void fixFields() {
        nowKM.setVisibility(View.GONE);
        type.setVisibility(View.GONE);
        when_to_remind.setVisibility(View.GONE);

        ConstraintLayout parent = (ConstraintLayout) findViewById(R.id.add_reminder_constraint_layout_inner);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parent);
        constraintSet.connect(mainKilometres.getId(),ConstraintSet.TOP,mainDate.getId(),ConstraintSet.BOTTOM,10);
        constraintSet.applyTo(parent);
    }

    /**
     * Fill fields with reminder data
     */
    private void fillFields() {
        if (reminder.getKm() == null) {
            //no km
            inputDate.getEditText().setText(sdfDate.format(reminder.getDate()));
            inputTime.getEditText().setText(sdfTime.format(reminder.getDate()));
        } else if (reminder.getDate() == null){
            //no date
            inputKM.getEditText().setText(reminder.getKm()+"");
        } else {
            //editing done reminder
            inputKM.getEditText().setText(reminder.getKm()+"");
            inputDate.getEditText().setText(sdfDate.format(reminder.getDate()));
            inputTime.getEditText().setText(sdfTime.format(reminder.getDate()));
        }
        inputTitle.getEditText().setText(reminder.getTitle());
        if (reminder.getDesc() == null || reminder.getDesc().equals("")) {}
        else
            inputDesc.getEditText().setText(reminder.getDesc());

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
}

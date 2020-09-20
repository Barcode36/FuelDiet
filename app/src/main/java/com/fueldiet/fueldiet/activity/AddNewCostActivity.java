package com.fueldiet.fueldiet.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddNewCostActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "AddNewCostActivity";

    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private TextInputLayout inputKM;
    private TextInputLayout inputTitle;
    private TextInputLayout inputPrice;
    private TextInputLayout inputDesc;
    private String displayType;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;
    private VehicleObject vehicle;

    private Switch resetKm;
    private Switch warranty;
    private Switch refund;

    private Calendar hidCalendar;
    Locale locale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost_new);

        Log.d(TAG, "onCreate: starting");

        Configuration configuration = getResources().getConfiguration();
        locale = configuration.getLocales().get(0);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.create_new_cost_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        vehicle = dbHelper.getVehicle(vehicleID);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

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

        warranty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inputPrice.getEditText().setText(getString(R.string.warranty));
                    inputPrice.setEnabled(false);
                    refund.setEnabled(false);
                } else {
                    inputPrice.getEditText().setText("");
                    inputPrice.setEnabled(true);
                    refund.setEnabled(true);
                }
            }
        });

        refund.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                warranty.setEnabled(false);
            else
                warranty.setEnabled(true);
        });

        /* Save button */
        FloatingActionButton addVehicle = findViewById(R.id.add_cost_save);
        addVehicle.setOnClickListener(v -> addNewCost());
        Log.d(TAG, "onCreate: finished");
    }

    /**
     * Create link between fields and variables
     */
    private void initVariables() {
        Log.d(TAG, "initVariables: started");
        inputDate = findViewById(R.id.add_cost_date_input);
        inputTime = findViewById(R.id.add_cost_time_input);
        Spinner inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.type_options, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputTypeSpinner.setAdapter(adapterS);
        inputTypeSpinner.setOnItemSelectedListener(this);
        inputTypeSpinner.setSelection(0);

        inputKM = findViewById(R.id.add_cost_km_input);
        inputPrice = findViewById(R.id.add_cost_total_cost_input);
        inputTitle = findViewById(R.id.add_cost_title_input);
        inputDesc = findViewById(R.id.add_cost_note_input);
        resetKm = findViewById(R.id.add_cost_reset_km);
        warranty = findViewById(R.id.add_cost_warranty_switch);
        refund = findViewById(R.id.add_cost_refund_switch);
        Log.d(TAG, "initVariables: finished");
    }

    /**
     * Save new cost
     */
    private void addNewCost() {
        Log.d(TAG, "addNewCost: started");
        CostObject co = new CostObject();

        String displayDate = inputDate.getEditText().getText().toString();
        String displayTime = inputTime.getEditText().getText().toString();

        if (!co.setKm(inputKM.getEditText().getText().toString())){
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return;
        }
        String cost = inputPrice.getEditText().getText().toString();
        if (warranty.isChecked())
            cost = "-80085";
        else if (refund.isChecked())
            cost = "-".concat(cost);

        if (!co.setCost(cost)){
            Toast.makeText(this, getString(R.string.insert_cost), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!co.setTitle(inputTitle.getEditText().getText().toString())){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        co.setDetails(inputDesc.getEditText().getText().toString());

        if (!co.setType(displayType)) {
            Toast.makeText(this, getString(R.string.select_cost), Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "addNewCost: all values are valid");

        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        Log.d(TAG, "addNewCost: created calendar");

        if (resetKm.getVisibility() == View.VISIBLE && resetKm.isChecked()) {
            co.setResetKm(1);
        } else {
            co.setResetKm(0);
        }

        co.setDate(c);
        co.setCarID(vehicleID);
        vehicle.setOdoCostKm(co.getKm());

        Log.d(TAG, "addNewCost: started checking if it can be inserted with km/date");
        List<CostObject> allCosts = dbHelper.getAllCosts(vehicleID);
        //List<CostObject> costs = new ArrayList<>();
        int i = 0;
        CostObject costObject = allCosts.get(i);
        CostObject bigger = null, smaller = null;
        while (costObject.getResetKm() != 0) {
            //costs.add(costObject);
            if (costObject.getKm() > co.getKm())
                bigger = costObject;
            if (costObject.getKm() < co.getKm() && smaller == null)
                smaller = costObject;
            costObject = allCosts.get(++i);
        }

        if (smaller != null) {
            //obstaja cost z manj km
            Log.d(TAG, "addNewCost: entry with less km exists");
            if (bigger != null) {
                //obstaja cost z manj in več km
                Log.d(TAG, "addNewCost: entry with more km exists");
                if (smaller.getDate().before(co.getDate())) {
                    //cost z manj km je časovno pred novim
                    Log.d(TAG, "addNewCost: entry with less km has smaller date");
                    if (bigger.getDate().after(co.getDate())) {
                        //cost z več km je tudi časovno kasneje
                        Log.d(TAG, "addNewCost: entry with more km has bigger date");
                        dbHelper.addCost(co);
                        if (co.getResetKm() == 1) {
                            vehicle.setOdoFuelKm(0);
                            vehicle.setOdoCostKm(0);
                            vehicle.setOdoRemindKm(0);
                        }
                        dbHelper.updateVehicle(vehicle);
                    }else {
                        Log.e(TAG, "addNewCost: entry with more km has smaller date");
                        Toast.makeText(this, getString(R.string.bigger_km_smaller_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    Log.e(TAG, "addNewCost: entry with less km has bigger date");
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                //ni večjega km kot trenutni
                Log.d(TAG, "addNewCost: entry with bigger km doesn't exists");
                if (smaller.getDate().before(co.getDate())) {
                    Log.d(TAG, "addNewCost: entry with less km has smaller date");
                    dbHelper.addCost(co);
                    if (co.getResetKm() == 1) {
                        vehicle.setOdoFuelKm(0);
                        vehicle.setOdoCostKm(0);
                        vehicle.setOdoRemindKm(0);
                    }
                    dbHelper.updateVehicle(vehicle);
                } else {
                    Log.e(TAG, "addNewCost: entry with less km has bigger date");
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            Log.d(TAG, "addNewCost: entry with smaller km doesn't exists");
            dbHelper.addCost(co);
            if (co.getResetKm() == 1) {
                vehicle.setOdoFuelKm(0);
                vehicle.setOdoCostKm(0);
                vehicle.setOdoRemindKm(0);
            }
            dbHelper.updateVehicle(vehicle);
        }
        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        Log.d(TAG, "addNewCost: finished");
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
    }


    /**
     * Updates calendar with new time
     * @param view view
     * @param hourOfDay selected hour
     * @param minute selected minutes
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, "onTimeSet");
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
        Log.d(TAG, "onDateSet");
        hidCalendar.set(Calendar.YEAR, year);
        hidCalendar.set(Calendar.MONTH, month);
        hidCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(hidCalendar.getTime());
        inputDate.getEditText().setText(date);
    }

    /**
     * Updates selected category
     * @param parent parent - dropdown
     * @param view view
     * @param position clicked item
     * @param id id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            displayType = null;
        } else {
            displayType = Utils.fromSLOtoENG(parent.getItemAtPosition(position).toString());

            if (parent.getItemAtPosition(position).toString().equals(getString(R.string.service)))
                resetKm.setVisibility(View.VISIBLE);
            else
                resetKm.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

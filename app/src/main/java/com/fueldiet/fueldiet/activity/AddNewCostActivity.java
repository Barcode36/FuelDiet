package com.fueldiet.fueldiet.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.TimeDatePickerHelper;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddNewCostActivity extends BaseActivity {

    private static final String TAG = "AddNewCostActivity";

    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private TextInputLayout inputKM;
    private TextInputLayout inputTitle;
    private TextInputLayout inputPrice;
    private TextInputLayout inputDesc;
    private TextInputLayout inputNewTotalKm;
    private String displayType;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;
    private VehicleObject vehicle;

    private SwitchMaterial resetKm;
    private SwitchMaterial warranty;
    private SwitchMaterial refund;

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
        vehicleID = intent.getLongExtra("vehicle_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        vehicle = dbHelper.getVehicle(vehicleID);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy", locale);
        sdfTime = new SimpleDateFormat("HH:mm", locale);

        Calendar hidCalendar = Calendar.getInstance();

        initVariables();

        /* Open time dialog */
        Objects.requireNonNull(inputTime.getEditText()).setOnClickListener(v -> {
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
        Objects.requireNonNull(inputDate.getEditText()).setOnClickListener(v -> {
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

        warranty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Objects.requireNonNull(inputPrice.getEditText()).setText(getString(R.string.warranty));
                inputPrice.setEnabled(false);
                refund.setEnabled(false);
            } else {
                Objects.requireNonNull(inputPrice.getEditText()).setText("");
                inputPrice.setEnabled(true);
                refund.setEnabled(true);
            }
        });

        refund.setOnCheckedChangeListener((buttonView, isChecked) -> warranty.setEnabled(!isChecked));

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
        AutoCompleteTextView inputTypeSpinner = findViewById(R.id.add_cost_category_autocomplete);

        Calendar calendar = Calendar.getInstance();
        Objects.requireNonNull(inputTime.getEditText()).setText(sdfTime.format(calendar.getTime()));
        Objects.requireNonNull(inputDate.getEditText()).setText(sdfDate.format(calendar.getTime()));

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.type_options, R.layout.list_item);
        adapterS.setDropDownViewResource(R.layout.list_item);
        inputTypeSpinner.setAdapter(adapterS);
        // TextWatcher is used for 'Material Spinner'
        inputTypeSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Only new value is needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Since this is a spinner value will change at once
            }

            @Override
            public void afterTextChanged(Editable s) {
                displayType = Utils.fromSLOtoENG(s.toString());
                Log.d(TAG, "onItemSelected: " + displayType);

                if (displayType.equals(getString(R.string.service))) {
                    resetKm.setVisibility(View.VISIBLE);
                } else {
                    resetKm.setVisibility(View.GONE);
                }
            }
        });

        inputKM = findViewById(R.id.add_cost_km_input);
        inputPrice = findViewById(R.id.add_cost_total_cost_input);
        inputTitle = findViewById(R.id.add_cost_title_input);
        inputDesc = findViewById(R.id.add_cost_note_input);
        resetKm = findViewById(R.id.add_cost_change_km);
        inputNewTotalKm = findViewById(R.id.add_cost_change_km_input);
        warranty = findViewById(R.id.add_cost_warranty_switch);
        refund = findViewById(R.id.add_cost_refund_switch);

        resetKm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                inputNewTotalKm.setVisibility(View.VISIBLE);
            else
                inputNewTotalKm.setVisibility(View.GONE);
        });

        Log.d(TAG, "initVariables: finished");
    }

    private CostObject createCostObject() {
        Log.d(TAG, "createCostObject");
        CostObject co = new CostObject();
        if (!co.setKm(Objects.requireNonNull(inputKM.getEditText()).getText().toString())){
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return null;
        }
        String cost = Objects.requireNonNull(inputPrice.getEditText()).getText().toString();
        if (warranty.isChecked())
            cost = "-80085";
        else if (refund.isChecked())
            cost = "-".concat(cost);

        if (!co.setCost(cost)){
            Toast.makeText(this, getString(R.string.insert_cost), Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!co.setTitle(Objects.requireNonNull(inputTitle.getEditText()).getText().toString())){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return null;
        }
        co.setDetails(Objects.requireNonNull(inputDesc.getEditText()).getText().toString());

        if (!co.setType(displayType)) {
            Toast.makeText(this, getString(R.string.select_cost), Toast.LENGTH_SHORT).show();
            return null;
        }

        if (resetKm.getVisibility() == View.VISIBLE && resetKm.isChecked()) {
            if (inputNewTotalKm.getEditText() == null) {
                Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
                return null;
            }
            co.setResetKm(Integer.parseInt(inputNewTotalKm.getEditText().getText().toString()));
        } else {
            co.setResetKm(-1);
        }
        co.setCarID(vehicleID);
        Log.d(TAG, "createCostObject: all values are valid");
        return co;
    }

    /**
     * Prepare new cost for saving
     */
    private void addNewCost() {
        Log.d(TAG, "addNewCost: started");

        String displayDate = Objects.requireNonNull(inputDate.getEditText()).getText().toString();
        String displayTime = Objects.requireNonNull(inputTime.getEditText()).getText().toString();

        CostObject co = createCostObject();

        if (co == null) {
            return;
        }
        int newTotalKmValue = co.getResetKm();

        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        co.setDate(c);
        vehicle.setOdoCostKm(co.getKm());

        Log.d(TAG, "addNewCost: started checking if it can be inserted with km/date");
        List<CostObject> allCosts = dbHelper.getAllCosts(vehicleID);
        int i = 0;
        CostObject costObject = allCosts.get(i);
        CostObject bigger = null;
        CostObject smaller = null;

        while (costObject.getResetKm() != -1) {
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
                        if (co.getResetKm() != -1) {
                            vehicle.setOdoFuelKm(newTotalKmValue);
                            vehicle.setOdoCostKm(newTotalKmValue);
                            vehicle.setOdoRemindKm(newTotalKmValue);
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
                    if (co.getResetKm() != -1) {
                        vehicle.setOdoFuelKm(newTotalKmValue);
                        vehicle.setOdoCostKm(newTotalKmValue);
                        vehicle.setOdoRemindKm(newTotalKmValue);
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
            if (co.getResetKm() != -1) {
                vehicle.setOdoFuelKm(newTotalKmValue);
                vehicle.setOdoCostKm(newTotalKmValue);
                vehicle.setOdoRemindKm(newTotalKmValue);
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
}

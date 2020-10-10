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
import android.widget.CompoundButton;
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
    private String displayType;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;
    private VehicleObject vehicle;

    private SwitchMaterial resetKm, warranty, refund;

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

        /* Open time dialog */
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
        AutoCompleteTextView inputTypeSpinner = (AutoCompleteTextView) findViewById(R.id.add_cost_category_autocomplete);

        Calendar calendar = Calendar.getInstance();
        inputTime.getEditText().setText(sdfTime.format(calendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(calendar.getTime()));

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.type_options, R.layout.list_item);
        adapterS.setDropDownViewResource(R.layout.list_item);
        inputTypeSpinner.setAdapter(adapterS);
        inputTypeSpinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                displayType = Utils.fromSLOtoENG(s.toString());
                Log.d(TAG, "onItemSelected: " + displayType);

                if (displayType.equals(getString(R.string.service)))
                    resetKm.setVisibility(View.VISIBLE);
                else
                    resetKm.setVisibility(View.INVISIBLE);
            }
        });

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
}

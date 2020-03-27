package com.fueldiet.fueldiet.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.fueldiet.fueldiet.fragment.DatePickerFragment;
import com.fueldiet.fueldiet.fragment.TimePickerFragment;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class EditCostActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private long costID;
    private CostObject costOld;
    private FuelDietDBHelper dbHelper;
    private long vehicleID;

    private TextInputLayout inputDate;
    private TextInputLayout inputTime;

    private TextInputLayout inputKM;
    private TextInputLayout inputTitle;
    private TextInputLayout inputPrice;
    private TextInputLayout inputDesc;
    private Spinner inputTypeSpinner;
    private String displayType;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private Switch resetKm;
    private Switch warranty;

    private Calendar hidCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_cost_title);

        Intent intent = getIntent();
        costID = intent.getLongExtra("cost_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        initVariables();
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

        warranty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inputPrice.getEditText().setText(getString(R.string.warranty));
                    inputPrice.setEnabled(false);
                } else {
                    inputPrice.getEditText().setText("");
                    inputPrice.setEnabled(true);
                }
            }
        });

        /* Save edit */
        FloatingActionButton addVehicle = findViewById(R.id.add_cost_save);
        addVehicle.setOnClickListener(v -> saveCostEdit());
    }

    /**
     * Connect variables with fields
     */
    private void initVariables() {
        inputDate = findViewById(R.id.add_cost_date_input);
        inputTime = findViewById(R.id.add_cost_time_input);
        inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

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
    }

    /**
     * Fill fields with cost
     */
    private void fillFields() {
        costOld = dbHelper.getCost(costID);
        vehicleID = costOld.getCarID();
        hidCalendar = costOld.getDate();

        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
        inputDate.getEditText().setText(sdfDate.format(hidCalendar.getTime()));

        inputDesc.getEditText().setText(costOld.getDetails());
        inputTitle.getEditText().setText(costOld.getTitle());
        inputPrice.getEditText().setText(String.valueOf(costOld.getCost()));
        inputKM.getEditText().setText(costOld.getKm()+"");

        String category = costOld.getType();
        List<String> dropDownCat = Arrays.asList(getResources().getStringArray(R.array.type_options));
        int position = dropDownCat.indexOf(category);
        if (position < 0) {
            position = dropDownCat.indexOf(Utils.fromENGtoSLO(category));
        }
        inputTypeSpinner.setSelection(position);
        if (costOld.getResetKm() == 1)
            resetKm.setChecked(true);
        else
            resetKm.setChecked(false);

        if ((dbHelper.getLastCost(vehicleID).getCostID() == costOld.getCostID() ||
                hidCalendar.after(dbHelper.getLastCost(vehicleID).getDate())) &&
                (dbHelper.getLastDrive(vehicleID) == null || hidCalendar.after(dbHelper.getLastDrive(vehicleID).getDate())) &&
                (dbHelper.getLatestDoneReminder(vehicleID) == null || hidCalendar.after(dbHelper.getLatestDoneReminder(vehicleID).getDate())))
            resetKm.setEnabled(true);
        else
            resetKm.setEnabled(false);

        if (costOld.getCost()+80085 == 0) {
            warranty.setChecked(true);
            inputPrice.getEditText().setText(getString(R.string.warranty));
            inputPrice.setEnabled(false);
        } else {
            warranty.setChecked(false);
            inputPrice.getEditText().setText("");
            inputPrice.setEnabled(true);
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

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
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

    /**
     * save cost edit
     */
    private void saveCostEdit() {
        CostObject co = new CostObject();
        co.setCarID(vehicleID);
        co.setCostID(costID);
        boolean ok = true;

        ok = ok && co.setKm(inputKM.getEditText().getText().toString());
        if (!ok){
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return;
        }
        String cost = inputPrice.getEditText().getText().toString();
        if (warranty.isChecked())
            cost = "-80085";
        ok = ok && co.setCost(cost);
        if (!ok){
            Toast.makeText(this, getString(R.string.insert_cost), Toast.LENGTH_SHORT).show();
            return;
        }

        ok = ok && co.setTitle(inputTitle.getEditText().getText().toString());
        if (!ok){
            Toast.makeText(this, getString(R.string.insert_title), Toast.LENGTH_SHORT).show();
            return;
        }
        ok = ok && co.setDetails(inputDesc.getEditText().getText().toString());

        ok = ok && co.setType(displayType);
        if (!ok) {
            Toast.makeText(this, getString(R.string.select_cost), Toast.LENGTH_SHORT).show();
            return;
        }

        if (resetKm.isChecked())
            co.setResetKm(1);
        else
            co.setResetKm(0);

        co.setDate(hidCalendar);

        VehicleObject vehicle = dbHelper.getVehicle(vehicleID);
        List<CostObject> withReset = dbHelper.getAllCostWithReset(vehicleID);
        Calendar afterThis = null;
        Calendar beforeThis = null;

        for (CostObject resetCost : withReset) {
            if (co.getDate().after(resetCost.getDate()))
                afterThis = resetCost.getDate();
            if (co.getDate().before(resetCost.getDate()))
                beforeThis = resetCost.getDate();
        }

        List<CostObject> allCosts;
        CostObject bigger = null, smaller = null;

        if (afterThis == null && beforeThis == null) {
            //we don't have any reset odo
            allCosts = dbHelper.getAllCosts(vehicleID);
            for (CostObject costObject : allCosts) {
                if (costObject.getKm() > co.getKm())
                    bigger = costObject;
                if (costObject.getKm() < co.getKm() && smaller == null)
                    smaller = costObject;
            }
        } else {
            CostObject tmp = dbHelper.getFirstCost(vehicleID);
            Calendar calendar = Calendar.getInstance();
            Calendar minus = tmp.getDate();
            minus.add(Calendar.YEAR, -1);
            long smallTime = afterThis == null ? minus.getTimeInMillis() / 1000 : afterThis.getTimeInMillis() / 1000;
            long bigTime = beforeThis == null ? calendar.getTimeInMillis() / 1000 : beforeThis.getTimeInMillis() / 1000;
            allCosts = dbHelper.getAllCostsWhereTimeBetween(vehicleID, smallTime, bigTime);
            for (CostObject costObject : allCosts) {
                if (costObject.getKm() > co.getKm())
                    bigger = costObject;
                if (costObject.getKm() < co.getKm() && smaller == null)
                    smaller = costObject;
            }
        }

        int kmFuel = dbHelper.getLastDrive(vehicleID) != null ? dbHelper.getLastDrive(vehicleID).getOdo() : 0;
        int kmCost = dbHelper.getLastCost(vehicleID) != null ? dbHelper.getLastCost(vehicleID).getKm() : 0;
        int kmRem = dbHelper.getLatestDoneReminder(vehicleID) != null ? dbHelper.getLatestDoneReminder(vehicleID).getKm() : 0;

        if (smaller != null) {
            //obstaja cost z manj km
            if (bigger != null) {
                //obstaja cost z manj in več km
                if (smaller.getDate().before(co.getDate())) {
                    //cost z manj km je časovno pred novim
                    if (bigger.getDate().after(co.getDate())) {
                        //cost z več km je tudi časovno kasneje
                        dbHelper.updateCost(co);
                        if (co.getResetKm() == 0 && costOld.getResetKm() == 1) {
                            vehicle.setOdoFuelKm(kmFuel);
                            vehicle.setOdoCostKm(kmCost);
                            vehicle.setOdoRemindKm(kmRem);
                        } else if (co.getResetKm() == 1 && costOld.getResetKm() == 0) {
                            vehicle.setOdoFuelKm(0);
                            vehicle.setOdoCostKm(0);
                            vehicle.setOdoRemindKm(0);
                        }
                        dbHelper.updateVehicle(vehicle);
                    }else {
                        Toast.makeText(this, getString(R.string.bigger_km_smaller_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                //ni večjega km kot trenutni
                if (smaller.getDate().before(co.getDate())) {
                    dbHelper.updateCost(co);
                    if (co.getResetKm() == 0) {
                        vehicle.setOdoFuelKm(kmFuel);
                        vehicle.setOdoCostKm(kmCost);
                        vehicle.setOdoRemindKm(kmRem);
                    } else if (co.getResetKm() == 1 && costOld.getResetKm() == 0) {
                        vehicle.setOdoFuelKm(0);
                        vehicle.setOdoCostKm(0);
                        vehicle.setOdoRemindKm(0);
                    }
                    dbHelper.updateVehicle(vehicle);
                } else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            dbHelper.updateCost(co);
            if (co.getResetKm() == 0) {
                vehicle.setOdoFuelKm(kmFuel);
                vehicle.setOdoCostKm(kmCost);
                vehicle.setOdoRemindKm(kmRem);
            } else if (co.getResetKm() == 1 && costOld.getResetKm() == 0) {
                vehicle.setOdoFuelKm(0);
                vehicle.setOdoCostKm(0);
                vehicle.setOdoRemindKm(0);
            }
            dbHelper.updateVehicle(vehicle);
        }


        /*
        CostObject min = dbHelper.getPrevCost(vehicleID, co.getKm());
        if (min != null) {
            //če obstaja manjša vrednost po km
            CostObject max = dbHelper.getNextCost(vehicleID, co.getKm());
            if (max != null) {
                //obstaja manjši in večji zapis, dajemo torej vmes
                if (min.getDate().before(co.getDate())) {
                    //tisti ki ima manj km, je tudi časovno prej
                    if (max.getDate().after(co.getDate())) {
                        //tisti ki ima več km je časovno kasneje
                        dbHelper.updateCost(co);
                    } else {
                        Toast.makeText(this, getString(R.string.bigger_km_smaller_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (min.getDate().before(co.getDate())) {
                    dbHelper.updateCost(co);
                } else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            dbHelper.updateCost(co);
        }*/
        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);
        finish();
    }
}

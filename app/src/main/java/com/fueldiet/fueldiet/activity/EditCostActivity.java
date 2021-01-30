package com.fueldiet.fueldiet.activity;

import android.content.Intent;
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
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.utils.TimeDatePickerHelper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class EditCostActivity extends BaseActivity {
    private static final String TAG = "AddNewCostActivity";
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
    private AutoCompleteTextView inputTypeSpinner;
    private String displayType;
    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    private SwitchMaterial resetKm, warranty, refund;

    private Calendar hidCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_cost_title);

        Intent intent = getIntent();
        costID = intent.getLongExtra("cost_id", 1);
        dbHelper = FuelDietDBHelper.getInstance(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        initVariables();
        fillFields();

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

        warranty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    inputPrice.getEditText().setText(getString(R.string.warranty));
                    inputPrice.setEnabled(false);
                    refund.setEnabled(false);
                } else {
                    refund.setEnabled(true);
                    if (costOld.getCost() < 0.0) {
                        inputPrice.getEditText().setText(String.valueOf(costOld.getCost()).replace("-", ""));
                        refund.setChecked(true);
                    } else {
                        inputPrice.getEditText().setText(String.valueOf(costOld.getCost()));
                        refund.setChecked(false);
                    }
                    inputPrice.setEnabled(true);

                }
            }
        });

        refund.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                warranty.setEnabled(false);
            else
                warranty.setEnabled(true);
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
        inputTypeSpinner = findViewById(R.id.add_cost_category_autocomplete);

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
        resetKm = findViewById(R.id.add_cost_change_km);
        warranty = findViewById(R.id.add_cost_warranty_switch);
        refund = findViewById(R.id.add_cost_refund_switch);
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
        inputKM.getEditText().setText(costOld.getKm()+"");

        String category = costOld.getType();
        List<String> dropDownCat = Arrays.asList(getResources().getStringArray(R.array.type_options));
        int position = dropDownCat.indexOf(category);
        if (position < 0) {
            position = dropDownCat.indexOf(Utils.fromENGtoSLO(category));
        }
        inputTypeSpinner.setText(dropDownCat.get(position), false);
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
        } else if (costOld.getCost() < 0.0) {
            refund.setChecked(true);
            //warranty.setChecked(false);
            inputPrice.getEditText().setText(String.valueOf(costOld.getCost()).replace("-", ""));
            inputPrice.setEnabled(true);
        } else {
            warranty.setChecked(false);
            inputPrice.getEditText().setText(String.valueOf(costOld.getCost()));
            inputPrice.setEnabled(true);
        }

    }

    @Override
    public void finish() {
        super.finish();
        AutomaticBackup automaticBackup = new AutomaticBackup(this);
        automaticBackup.createBackup(this);
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
        else if (refund.isChecked() && !cost.contains("-"))
            cost = "-".concat(cost);
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

        co.setResetKm(costOld.getResetKm());

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

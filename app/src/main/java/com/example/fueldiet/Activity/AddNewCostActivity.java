package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.DialogFragment;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.example.fueldiet.Object.CostObject;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewCostActivity extends BaseActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {
    private long vehicleID;
    private FuelDietDBHelper dbHelper;

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

    private Calendar hidCalendar;

    /*
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_cost_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        hidCalendar = Calendar.getInstance();

        setVariables();

        inputTime.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.setArguments(currentDate);
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.getEditText().setOnClickListener(v -> {
            Bundle currentDate = new Bundle();
            currentDate.putLong("date", hidCalendar.getTimeInMillis());
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.setArguments(currentDate);
            datePicker.show(getSupportFragmentManager(), "date picker");
        });


        FloatingActionButton addVehicle = findViewById(R.id.add_cost_save);
        addVehicle.setOnClickListener(v -> addNewCost());
    }

    private void setVariables() {
        inputDate = findViewById(R.id.add_cost_date_input);
        inputTime = findViewById(R.id.add_cost_time_input);
        inputTypeSpinner = findViewById(R.id.add_reminder_mode_spinner);

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
    }

    private void addNewCost() {
        CostObject co = new CostObject();
        boolean ok = true;
        String displayDate = inputDate.getEditText().getText().toString();
        String displayTime = inputTime.getEditText().getText().toString();

        ok = ok && co.setKm(inputKM.getEditText().getText().toString());
        if (!ok){
            Toast.makeText(this, getString(R.string.insert_km), Toast.LENGTH_SHORT).show();
            return;
        }
        ok = ok && co.setCost(inputPrice.getEditText().getText().toString());
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

        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        co.setDate(c);
        co.setCarID(vehicleID);

        //Cursor min = dbHelper.getPrevCostOld(vehicleID, co.getKm());
        CostObject min = dbHelper.getPrevCost(vehicleID, co.getKm());
        if (min != null) {
            //če obstaja manjša vrednost po km
            CostObject max = dbHelper.getNextCost(vehicleID, co.getKm());
            if (max != null) {
                //obstaja manjši in večji zapis, dajemo torej vmes
                //if (Long.parseLong(min.getString(min.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE))) < (c.getTimeInMillis() / 1000)) {
                if (min.getDate().before(co.getDate())) {
                    //tisti ki ima manj km, je tudi časovno prej
                    if (max.getDate().after(co.getDate())) {
                        //tisti ki ima več km je časovno kasneje
                        //dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, displayType, (c.getTimeInMillis() / 1000));
                        dbHelper.addCost(co);
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
                    dbHelper.addCost(co);
                } else {
                    Toast.makeText(this, getString(R.string.smaller_km_bigger_time), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            dbHelper.addCost(co);
        }

        Utils.checkKmAndSetAlarms(vehicleID, dbHelper, this);

        Intent intent = new Intent(AddNewCostActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        intent.putExtra("frag", 1);
        //startActivity(intent);
        finish();
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hidCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        hidCalendar.set(Calendar.MINUTE, minute);
        inputTime.getEditText().setText(sdfTime.format(hidCalendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        hidCalendar.set(Calendar.YEAR, year);
        hidCalendar.set(Calendar.MONTH, month);
        hidCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(hidCalendar.getTime());
        inputDate.getEditText().setText(date);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            displayType = null;
        } else {
            displayType = Utils.fromSLOtoENG(parent.getItemAtPosition(position).toString());
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

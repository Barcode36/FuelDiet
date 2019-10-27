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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.TimePickerFragment;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewCostActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost_new);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        setVariables();

        inputTime.getEditText().setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.getEditText().setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
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
        String displayDate = inputDate.getEditText().getText().toString();
        String displayTime = inputTime.getEditText().getText().toString();
        if (inputKM.getEditText().getText().toString().equals("")){
            Toast.makeText(this, "Please insert kilometres", Toast.LENGTH_SHORT).show();
            return;
        }
        int displayKm = Integer.parseInt(inputKM.getEditText().getText().toString());
        if (inputPrice.getEditText().getText().toString().equals("")){
            Toast.makeText(this, "Please insert cost", Toast.LENGTH_SHORT).show();
            return;
        }
        double displayPrice = Double.parseDouble(inputPrice.getEditText().getText().toString());
        String displayTitle = inputTitle.getEditText().getText().toString();
        if (inputTitle.getEditText().getText().toString().equals("")){
            Toast.makeText(this, "Please insert title", Toast.LENGTH_SHORT).show();
            return;
        }
        String displayDesc = inputDesc.getEditText().getText().toString();
        if (displayDesc.equals(""))
            displayDesc = null;

        if (displayType == null) {
            Toast.makeText(this, "Please select type of cost!", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));

        Cursor min = dbHelper.getPrevCost(vehicleID, displayKm);
        if (min != null) {
            //če obstaja manjša vrednost po km
            Cursor max = dbHelper.getNextCost(vehicleID, displayKm);
            if (max != null) {
                //obstaja manjši in večji zapis, dajemo torej vmes
                if (Long.parseLong(min.getString(min.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE))) < (c.getTimeInMillis() / 1000)) {
                    //tisti ki ima manj km, je tudi časovno prej
                    long time1 = Long.parseLong(max.getString(max.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE)));
                    long timeNow = c.getTimeInMillis()/1000;
                    if (Long.parseLong(max.getString(max.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE))) > (c.getTimeInMillis() / 1000)) {
                        //tisti ki ima več km je časovno kasneje
                        dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, displayType, (c.getTimeInMillis() / 1000));
                    } else {
                        Toast.makeText(this, "Entry with greater km has earlier date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, "Entry with lesser km has later date", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (Long.parseLong(min.getString(min.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE))) < (c.getTimeInMillis() / 1000)) {
                    dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, displayType, (c.getTimeInMillis() / 1000));
                } else {
                    Toast.makeText(this, "Entry with lesser km has later date", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, displayType, (c.getTimeInMillis() / 1000));
        }

        Intent intent = new Intent(AddNewCostActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        intent.putExtra("frag", 1);
        startActivity(intent);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        inputTime.getEditText().setText(hourOfDay+":"+minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(calendar.getTime());
        inputDate.getEditText().setText(date);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            displayType = null;
        } else {
            displayType = parent.getItemAtPosition(position).toString();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddNewCostActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private EditText inputDate;
    private EditText inputTime;

    private EditText inputKM;
    private EditText inputTitle;
    private EditText inputPrice;
    private EditText inputDesc;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("vehicle_id", vehicleID);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    SimpleDateFormat sdfDate;
    SimpleDateFormat sdfTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_cost);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.create_new_drive_title);

        Intent intent = getIntent();
        vehicleID = intent.getLongExtra("vehicle_id", (long)1);
        dbHelper = new FuelDietDBHelper(this);

        sdfDate = new SimpleDateFormat("dd.MM.yyyy");
        sdfTime = new SimpleDateFormat("HH:mm");

        setVariables();

        inputTime.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });
        inputDate.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });


        FloatingActionButton addVehicle = findViewById(R.id.add_cost_save);
        addVehicle.setOnClickListener(v -> addNewCost());
    }

    private void setVariables() {
        inputDate = findViewById(R.id.add_cost_date_input);
        inputTime = findViewById(R.id.add_cost_time_input);

        Calendar calendar = Calendar.getInstance();
        inputTime.setText(sdfTime.format(calendar.getTime()));
        inputDate.setText(sdfDate.format(calendar.getTime()));

        inputKM = findViewById(R.id.add_cost_km_input);
        inputPrice = findViewById(R.id.add_cost_price_input);
        inputTitle = findViewById(R.id.add_cost_time_input);
        inputDesc = findViewById(R.id.add_cost_desc_input);
    }

    private void addNewCost() {
        String displayDate = inputDate.getText().toString();
        String displayTime = inputTime.getText().toString();
        int displayKm = Integer.parseInt(inputKM.getText().toString());
        double displayPrice = Double.parseDouble(inputPrice.getText().toString());
        String displayTitle = inputTitle.getText().toString();
        String displayDesc = inputDesc.getText().toString();


        Calendar c = Calendar.getInstance();
        String [] date = displayDate.split("\\.");
        String [] time = displayTime.split(":");
        c.set(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]));
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
                    if (Long.parseLong(max.getString(max.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE))) > (c.getTimeInMillis() / 1000)) {
                        //tisti ki ima več km je časovno kasneje
                        dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, String.valueOf(c.getTimeInMillis() / 1000));
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
                    dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, String.valueOf(c.getTimeInMillis() / 1000));
                } else {
                    Toast.makeText(this, "Entry with lesser km has later date", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            dbHelper.addCost(vehicleID, displayPrice, displayTitle, displayKm, displayDesc, String.valueOf(c.getTimeInMillis() / 1000));
        }

        Intent intent = new Intent(AddNewCostActivity.this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", vehicleID);
        startActivity(intent);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        inputTime.setText(hourOfDay+":"+minute);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String date = sdfDate.format(calendar.getTime());
        inputDate.setText(date);
    }
}

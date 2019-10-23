package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.MonthYearPickerFragment;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    private long vehicle_id;
    private Spinner spinnerType;
    private FuelDietDBHelper dbHelper;
    private EditText fromDate;
    private EditText toDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_chart);

        fromDate = findViewById(R.id.vehicle_chart_from_date);
        toDate = findViewById(R.id.vehicle_chart_to_date);

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Charts view");
        spinnerType = findViewById(R.id.vehicle_chart_spinner_type);
        dbHelper = new FuelDietDBHelper(this);



        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.chart_options, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterS);
        spinnerType.setOnItemSelectedListener(this);

        fromDate.setOnClickListener(v -> {
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment();
            newFragment.setValueChangeListener(this);
            newFragment.show(getSupportFragmentManager(), "time picker");
        });
        toDate.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        findViewById(R.id.vehicle_chart_progress_bar).setVisibility(View.VISIBLE);
        switch (position) {
            case 0:

                break;
            case 1:
                createPie();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerType.setSelection(0);
    }

    private void createPie() {
        Pie pie = AnyChart.pie();

        Cursor c = dbHelper.getAllCosts(vehicle_id);

        String[] keys = getResources().getStringArray(R.array.type_options);
        Map<String, Double> costs = new HashMap<>();
        for (String key : keys)
            costs.put(key, 0.0);

        try {
            while (c.moveToNext()) {
                String tmp = c.getString(c.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TYPE));
                double tmpPrice = c.getDouble(c.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_EXPENSE));
                Double value = costs.get(tmp);
                value += tmpPrice;
                costs.put(tmp, value);
            }
        } finally {
            c.close();
        }


        List<DataEntry> data = new ArrayList<>();
        for (String key : costs.keySet())
            if (Double.compare(costs.get(key), 0.0) > 0)
                data.add(new ValueDataEntry(key, costs.get(key)));


        pie.data(data);

        AnyChartView anyChartView = findViewById(R.id.vehicle_chart_view);
        anyChartView.setChart(pie);
        anyChartView.setProgressBar(findViewById(R.id.vehicle_chart_progress_bar));
        //findViewById(R.id.vehicle_chart_loading_panel).setVisibility(View.GONE);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.e("MY", "Old "+oldVal+" new: "+newVal);
    }
}

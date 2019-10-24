package com.example.fueldiet.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fueldiet.Fragment.MonthYearPickerFragment;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    private long vehicle_id;
    private Spinner spinnerType;
    private FuelDietDBHelper dbHelper;
    private EditText fromDate;
    private EditText toDate;
    private String which;
    private Button whichTypes;
    private Button showChart;

    private String smallestEpoch;
    private String biggestEpoch;

    private String smallEpoch;
    private String bigEpoch;

    private int spinnerPosition;

    private List<String> excludeType = new ArrayList<>();

    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_chart);

        fromDate = findViewById(R.id.vehicle_chart_from_date);
        toDate = findViewById(R.id.vehicle_chart_to_date);
        whichTypes = findViewById(R.id.vehicle_chart_select_types);

        pieChart = findViewById(R.id.vehicle_chart);
        pieChart.setNoDataText("");

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Charts view");
        spinnerType = findViewById(R.id.vehicle_chart_spinner_type);
        showChart = findViewById(R.id.vehicle_chart_show);
        dbHelper = new FuelDietDBHelper(this);

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.chart_options, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterS);
        spinnerType.setOnItemSelectedListener(this);

        //setUpTimePeriod();

        fromDate.setOnClickListener(v -> {
            which = "from";
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment();
            newFragment.setValueChangeListener(this);
            newFragment.show(getSupportFragmentManager(), "time picker");
        });
        toDate.setOnClickListener(v -> {
            which = "to";
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment();
            newFragment.setValueChangeListener(this);
            newFragment.show(getSupportFragmentManager(), "time picker");
        });

        showChart.setOnClickListener(v -> {
            switch (spinnerPosition) {
                case 0:
                    break;
                case 1:
                    setUpPie();
                    break;
            }
        });

        whichTypes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ModelChartActivity.this);
            String [] tmpTypes = getResources().getStringArray(R.array.type_options);
            String [] types = new String[6];
            for (int z = 1; z < tmpTypes.length; z++)
                types[z-1] = tmpTypes[z];
            boolean[] checkedTypes = new boolean[]{
                   true, true, true, true, true, true
            };
            final List<String> typesList = Arrays.asList(types);
            for (String alreadySet : excludeType) {
                checkedTypes[typesList.indexOf(alreadySet)] = false;
            }
            excludeType = new ArrayList<>();
            builder.setMultiChoiceItems(types, checkedTypes, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checkedTypes[which] = isChecked;
                }
            });
            builder.setTitle("Which types to include in chart?");
            builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < checkedTypes.length; i++)
                        if (!checkedTypes[i])
                            excludeType.add(typesList.get(i));
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    smallEpoch = null;
                    bigEpoch = null;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    public void setUpTimePeriod() {
        if (spinnerPosition == 0) {
            smallestEpoch = dbHelper.getFirstDrive(vehicle_id);
            biggestEpoch = dbHelper.getLastDrive(vehicle_id);
        } else if (spinnerPosition == 1) {
            smallestEpoch = dbHelper.getFirstCost(vehicle_id);
            biggestEpoch = dbHelper.getLastCost(vehicle_id);
        }
        Calendar calendar = Calendar.getInstance();
        if (smallEpoch == null || bigEpoch == null) {
            calendar.setTimeInMillis(Long.parseLong(smallestEpoch) * 1000);
            fromDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(biggestEpoch) * 1000);
            toDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
        } else {
            calendar.setTimeInMillis(Long.parseLong(smallEpoch) * 1000);
            fromDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
            calendar.setTimeInMillis(Long.parseLong(bigEpoch) * 1000);
            toDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                spinnerPosition = 0;
                setUpTimePeriod();
                whichTypes.setVisibility(View.GONE);
                break;
            case 1:
                spinnerPosition = 1;
                whichTypes.setVisibility(View.VISIBLE);
                setUpTimePeriod();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerType.setSelection(0);
    }


    public void setUpPie() {
        Cursor c;
        if (smallEpoch == null || bigEpoch == null)
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, smallestEpoch, biggestEpoch);
        else
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, smallEpoch, bigEpoch);

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

        List<PieEntry> entries = new ArrayList<>();

        for (String key : costs.keySet()) {
            if (Double.compare(costs.get(key), 0.0) > 0) {
                if (!excludeType.contains(key)) {
                    entries.add(new PieEntry(costs.get(key).floatValue(), key));
                }
            }
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(ColorTemplate.COLORFUL_COLORS);
        set.setSelectionShift(30);
        PieData data = new PieData(set);
        data.setValueTextSize(20f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter());
        pieChart.setData(data);
        pieChart.animateY(500);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        pieChart.setUsePercentValues(true);
        pieChart.setTouchEnabled(false);

        //replaced with legend
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //picker is NULL!!!!, oldVal is month, newVal is year
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.MONTH, oldVal-1);
        calendar.set(Calendar.YEAR, newVal);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (which.equals("from")) {
            smallEpoch = String.valueOf(calendar.getTimeInMillis() / 1000);
            fromDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
        } else {
            bigEpoch = String.valueOf(calendar.getTimeInMillis() / 1000);
            toDate.setText(String.format("%d. %d", calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)));
        }
        which = null;
    }
}

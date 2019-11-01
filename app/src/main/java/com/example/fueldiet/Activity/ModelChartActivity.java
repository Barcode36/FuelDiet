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
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ModelChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    private long vehicle_id;
    private Spinner spinnerType;
    private FuelDietDBHelper dbHelper;
    private EditText fromDate;
    private EditText toDate;
    private String which;
    private Button whichTypes;
    private Button showChart;

    private Calendar smallestEpoch;
    private Calendar biggestEpoch;

    private Calendar smallEpoch;
    private Calendar bigEpoch;

    private int spinnerPosition;

    private List<String> excludeType = new ArrayList<>();

    private PieChart pieChart;
    private LineChart lineChart;

    SimpleDateFormat sdfDate = new SimpleDateFormat("MM. yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_chart);

        fromDate = findViewById(R.id.vehicle_chart_from_date);
        toDate = findViewById(R.id.vehicle_chart_to_date);
        whichTypes = findViewById(R.id.vehicle_chart_select_types);

        pieChart = findViewById(R.id.vehicle_chart_pie);
        pieChart.setNoDataText("PieChart");
        lineChart = findViewById(R.id.vehicle_chart_line);
        lineChart.setNoDataText("LineChart");

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

        fromDate.setOnClickListener(v -> {
            which = "from";
            int[] dt = getMYfromDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getSupportFragmentManager(), "time picker");
        });
        toDate.setOnClickListener(v -> {
            which = "to";
            int[] dt = getMYtoDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getSupportFragmentManager(), "time picker");
        });

        showChart.setOnClickListener(v -> {
            switch (spinnerPosition) {
                case 0:
                    setUpLine();
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
            String epochSecMin = dbHelper.getFirstDrive(vehicle_id);
            smallestEpoch = Calendar.getInstance();
            smallestEpoch.setTimeInMillis(Long.parseLong(epochSecMin)*1000);
            smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
            smallestEpoch.set(Calendar.HOUR, 1);
            smallestEpoch.set(Calendar.MINUTE, 1);

            String epochSecMax = dbHelper.getLastDrive(vehicle_id);
            biggestEpoch = Calendar.getInstance();
            biggestEpoch.setTimeInMillis(Long.parseLong(epochSecMax)*1000);
            int z = biggestEpoch.getActualMaximum(Calendar.DAY_OF_MONTH);
            biggestEpoch.set(Calendar.DAY_OF_MONTH, biggestEpoch.getActualMaximum(Calendar.DAY_OF_MONTH));
            biggestEpoch.set(Calendar.HOUR_OF_DAY, 23);
            biggestEpoch.set(Calendar.MINUTE, 55);
        } else if (spinnerPosition == 1) {
            String epochSecMin = dbHelper.getFirstCost(vehicle_id);
            smallestEpoch = Calendar.getInstance();
            smallestEpoch.setTimeInMillis(Long.parseLong(epochSecMin)*1000);
            smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
            smallestEpoch.set(Calendar.HOUR, 1);
            smallestEpoch.set(Calendar.MINUTE, 1);

            String epochSecMax = dbHelper.getLastCost(vehicle_id);
            biggestEpoch = Calendar.getInstance();
            biggestEpoch.setTimeInMillis(Long.parseLong(epochSecMax)*1000);
            biggestEpoch.set(Calendar.DAY_OF_MONTH, biggestEpoch.getActualMaximum(Calendar.DAY_OF_MONTH));
            biggestEpoch.set(Calendar.HOUR_OF_DAY, 23);
            biggestEpoch.set(Calendar.MINUTE, 55);
        }

        if (smallEpoch == null && bigEpoch == null) {
            fromDate.setText(sdfDate.format(smallestEpoch.getTime()));
            toDate.setText(sdfDate.format(biggestEpoch.getTime()));
        } else if (smallEpoch == null){
            fromDate.setText(sdfDate.format(smallestEpoch.getTime()));
            toDate.setText(sdfDate.format(bigEpoch.getTime()));
        } else {
            fromDate.setText(sdfDate.format(smallEpoch.getTime()));
            toDate.setText(sdfDate.format(biggestEpoch.getTime()));
        }
    }

    public int[] getMYfromDate() {
        int [] my = new int[2];
        my[0] = Integer.parseInt(fromDate.getText().toString().split("\\.")[0].trim());
        my[1] = Integer.parseInt(fromDate.getText().toString().split("\\.")[1].trim());
        return my;
    }
    public int[] getMYtoDate() {
        int [] my = new int[2];
        my[0] = Integer.parseInt(toDate.getText().toString().split("\\.")[0].trim());
        my[1] = Integer.parseInt(toDate.getText().toString().split("\\.")[1].trim());
        return my;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = dbHelper.getAllCosts(vehicle_id);
        switch (position) {
            case 0:
                spinnerPosition = 0;
                if (cursor.getCount() > 0)
                    setUpTimePeriod();
                pieChart.clear();
                pieChart.setVisibility(View.INVISIBLE);
                lineChart.setVisibility(View.VISIBLE);
                whichTypes.setVisibility(View.GONE);
                break;
            case 1:
                spinnerPosition = 1;
                whichTypes.setVisibility(View.VISIBLE);
                pieChart.setVisibility(View.VISIBLE);
                lineChart.clear();
                lineChart.setVisibility(View.INVISIBLE);
                if (cursor.getCount() > 0)
                    setUpTimePeriod();
                break;
        }
        cursor.close();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerType.setSelection(0);
    }


    public void setUpPie() {
        Cursor c;
        if (smallEpoch == null && bigEpoch == null) {
            long epochMIN = (smallestEpoch.getTimeInMillis()/1000);
            long epochMAX = (biggestEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else if (smallEpoch == null) {
            long epochMIN = (smallestEpoch.getTimeInMillis()/1000);
            long epochMAX = (bigEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else if (bigEpoch == null){
            long epochMIN = (smallEpoch.getTimeInMillis()/1000);
            long epochMAX = (biggestEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else {
            long epochMIN = (smallEpoch.getTimeInMillis()/1000);
            long epochMAX = (bigEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllCostsWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        }

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
        pieChart.setHighlightPerTapEnabled(false);

        //replaced with legend
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

    public void setUpLine() {
        List<Entry> consumptionValues = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        Cursor c;
        if (smallEpoch == null && bigEpoch == null) {
            long epochMIN = (smallestEpoch.getTimeInMillis()/1000);
            long epochMAX = (biggestEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllDrivesWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else if (smallEpoch == null) {
            long epochMIN = (smallestEpoch.getTimeInMillis()/1000);
            long epochMAX = (bigEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllDrivesWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else if (bigEpoch == null){
            long epochMIN = (smallEpoch.getTimeInMillis()/1000);
            long epochMAX = (biggestEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllDrivesWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        } else {
            long epochMIN = (smallEpoch.getTimeInMillis()/1000);
            long epochMAX = (bigEpoch.getTimeInMillis()/1000);
            c = dbHelper.getAllDrivesWhereTimeBetween(vehicle_id, epochMIN, epochMAX);
        }
        int counter = 0;
        try {
            while (c.moveToNext()) {
                String timedate = c.getString(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE));
                int trip = c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM));
                double litres = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES));
                double cons = Utils.calculateConsumption(trip, litres);
                consumptionValues.add(new Entry((float)counter, (float)cons));
                dates.add(timedate);
                counter++;
            }
        } finally {
            c.close();
        }
        LineDataSet set1 = new LineDataSet(consumptionValues, "Consumption");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.invalidate(); // refresh

    }


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //picker is NULL!!!!, oldVal is month, newVal is year
        Calendar calendar = Calendar.getInstance();
        //calendar.set(newVal, oldVal-1, 1, 2,1);

        if (which.equals("from")) {
            calendar.set(newVal, oldVal-1, 1, 1,1);
            smallEpoch = calendar;
            fromDate.setText(sdfDate.format(smallEpoch.getTime()));
        } else {
            calendar.set(newVal, oldVal-1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23,59);
            bigEpoch = calendar;
            toDate.setText(sdfDate.format(bigEpoch.getTime()));
        }
        which = null;
    }
}

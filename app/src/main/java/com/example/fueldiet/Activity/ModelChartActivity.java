package com.example.fueldiet.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import androidx.fragment.app.DialogFragment;


import com.example.fueldiet.Fragment.DatePickerFragment;
import com.example.fueldiet.Fragment.MonthYearPickerFragment;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.highsoft.highcharts.common.hichartsclasses.HIChart;
import com.highsoft.highcharts.common.hichartsclasses.HIDataLabels;
import com.highsoft.highcharts.common.hichartsclasses.HIOptions;
import com.highsoft.highcharts.common.hichartsclasses.HIPie;
import com.highsoft.highcharts.common.hichartsclasses.HIPlotOptions;
import com.highsoft.highcharts.common.hichartsclasses.HITitle;
import com.highsoft.highcharts.common.hichartsclasses.HITooltip;
import com.highsoft.highcharts.core.HIChartView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ModelChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    private long vehicle_id;
    private Spinner spinnerType;
    private FuelDietDBHelper dbHelper;
    private EditText fromDate;
    private EditText toDate;
    private String which;
    private Button whichTypes;
    private Button showChart;

    private int spinnerPosition;

    private List<String> excludeType = new ArrayList<>();


    HIOptions options;
    HIChartView chartView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_chart);

        fromDate = findViewById(R.id.vehicle_chart_from_date);
        toDate = findViewById(R.id.vehicle_chart_to_date);
        whichTypes = findViewById(R.id.vehicle_chart_select_types);

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Charts view");
        spinnerType = findViewById(R.id.vehicle_chart_spinner_type);
        showChart = findViewById(R.id.vehicle_chart_show);
        dbHelper = new FuelDietDBHelper(this);

        String first = dbHelper.getFirstCost(vehicle_id);
        String last = dbHelper.getLastCost(vehicle_id);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(first)*1000);

        fromDate.setText(calendar.get(Calendar.MONTH) + ". " + calendar.get(Calendar.YEAR));
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(last)*1000);

        toDate.setText(calendar.get(Calendar.MONTH) + ". " + calendar.get(Calendar.YEAR));



        chartView = findViewById(R.id.vehicle_chart_view);
        options = new HIOptions();


        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.chart_options, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapterS);
        spinnerType.setOnItemSelectedListener(this);

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

        showChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (spinnerPosition) {
                    case 0:
                        break;
                    case 1:
                        createPie();
                        break;
                }
            }
        });

        whichTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //findViewById(R.id.vehicle_chart_progress_bar).setVisibility(View.VISIBLE);
        switch (position) {
            case 0:
                spinnerPosition = 0;
                break;
            case 1:
                spinnerPosition = 1;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerType.setSelection(0);
    }


    public void createPie() {
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
        /*
        HIChart chart = new HIChart();
        chart.setType("pie");
        chart.setBackgroundColor(null);
        chart.setPlotBorderWidth(null);
        chart.setPlotShadow(false);
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Browser market shares January, 2015 to May, 2015");
        options.setTitle(title);

        HITooltip tooltip = new HITooltip();
        tooltip.setPointFormat("{series.name}: <b>{point.percentage:.1f}%</b>");
        options.setTooltip(tooltip);

        HIPlotOptions plotOptions = new HIPlotOptions();
        plotOptions.setPie(new HIPie());
        plotOptions.getPie().setAllowPointSelect(true);
        plotOptions.getPie().setCursor("pointer");
        plotOptions.getPie().setShowInLegend(true);
        options.setPlotOptions(plotOptions);

        HIPie series1 = new HIPie();
        series1.setName("Brands");

        //series1.setData(new ArrayList<>(Arrays.asList(map1, map2, map3, map4, map5, map6)));

        List<Map<String, Object>> data = new ArrayList<>();
        for (String key : costs.keySet()) {
            if (Double.compare(costs.get(key), 0.0) > 0) {
                if (!excludeType.contains(key)) {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("string", key);
                    tmp.put("y", costs.get(key));
                    data.add(tmp);
                }
            }
        }

        series1.setData(new ArrayList<>(Arrays.asList(data)));
        options.setSeries(new ArrayList<>(Arrays.asList(series1)));

        chartView.setOptions(options);

         */

        HIOptions options = new HIOptions();

        HIChart chart = new HIChart();
        chart.setType("pie");
        chart.setBackgroundColor(null);
        chart.setPlotBorderWidth(null);
        chart.setPlotShadow(false);
        options.setChart(chart);

        HITitle title = new HITitle();
        title.setText("Browser market shares January, 2015 to May, 2015");
        options.setTitle(title);

        HITooltip tooltip = new HITooltip();
        tooltip.setPointFormat("{series.name}: <b>{point.percentage:.1f}%</b>");
        options.setTooltip(tooltip);

        HIPlotOptions plotOptions = new HIPlotOptions();
        plotOptions.setPie(new HIPie());
        plotOptions.getPie().setAllowPointSelect(true);
        plotOptions.getPie().setCursor("pointer");
        plotOptions.getPie().setShowInLegend(true);
        options.setPlotOptions(plotOptions);

        HIPie series1 = new HIPie();
        series1.setName("Brands");

        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("name", "Microsoft Internet Explorer");
        map1.put("y", 56.33);

        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("name", "Chrome");
        map2.put("y", 24.03);
        map2.put("sliced", true);
        map2.put("selected", true);

        HashMap<String, Object> map3 = new HashMap<>();
        map3.put("name", "Firefox");
        map3.put("y", 10.38);

        HashMap<String, Object> map4 = new HashMap<>();
        map4.put("name", "Safari");
        map4.put("y", 4.77);

        HashMap<String, Object> map5 = new HashMap<>();
        map5.put("name", "Opera");
        map5.put("y", 0.91);

        HashMap<String, Object> map6 = new HashMap<>();
        map6.put("name", "Proprietary or Undetectable");
        map6.put("y", 0.2);

        series1.setData(new ArrayList<>(Arrays.asList(map1, map2, map3, map4, map5, map6)));

        options.setSeries(new ArrayList<>(Arrays.asList(series1)));

        chartView.setOptions(options);

    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //picker is NULL!!!!, oldVal is month, newVal is year
        if (which.equals("from"))
            fromDate.setText(oldVal+". "+newVal);
        else
            toDate.setText(oldVal+". "+newVal);
        which = null;
    }
}

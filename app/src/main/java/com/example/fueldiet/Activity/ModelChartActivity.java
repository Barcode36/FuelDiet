package com.example.fueldiet.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.example.fueldiet.R;

import java.util.ArrayList;
import java.util.List;

public class ModelChartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private long vehicle_id;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_chart);

        Intent intent = getIntent();
        vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Charts view");
        spinner = findViewById(R.id.vehicle_chart_spinner);

        ArrayAdapter<CharSequence> adapterS = ArrayAdapter.createFromResource(this,
                R.array.chart_options, android.R.layout.simple_spinner_item);
        adapterS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterS);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        findViewById(R.id.vehicle_chart_progress_bar).setVisibility(View.VISIBLE);
        switch (position) {
            case 0:
                //createPie();
                break;
            case 1:
                createPie();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(0);
    }

    private void createPie() {
        Pie pie = AnyChart.pie();

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("John", 10000));
        data.add(new ValueDataEntry("Jake", 12000));
        data.add(new ValueDataEntry("Peter", 18000));

        pie.data(data);

        AnyChartView anyChartView = findViewById(R.id.vehicle_chart_view);
        anyChartView.setChart(pie);
        anyChartView.setProgressBar(findViewById(R.id.vehicle_chart_progress_bar));
        //findViewById(R.id.vehicle_chart_loading_panel).setVisibility(View.GONE);
    }
}

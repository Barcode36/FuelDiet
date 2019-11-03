package com.example.fueldiet.Fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.MyMarkerView;
import com.example.fueldiet.MyValueAxisFormatter;
import com.example.fueldiet.MyValueFormatter;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarChartFragment extends Fragment implements OnChartValueSelectedListener {

    public BarChartFragment() { }

    private long vehicleID;
    private FuelDietDBHelper dbHelper;
    private BarChart barChart;
    private String which;
    private List<String> excludeType;

    public static BarChartFragment newInstance(long id) {
        BarChartFragment fragment = new BarChartFragment();
        Bundle args = new Bundle();
        args.putLong("vehicleID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleID = getArguments().getLong("vehicleID");
        }
        dbHelper = new FuelDietDBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);

        Button whichTypes = view.findViewById(R.id.vehicle_chart_bar_select_types);
        barChart = view.findViewById(R.id.vehicle_chart_bar);
        barChart.setNoDataText("BarChart is waiting...");
        barChart.setOnChartValueSelectedListener(this);

        excludeType = new ArrayList<>();
        excludeType.addAll(Arrays.asList(getResources().getStringArray(R.array.type_options)));
        excludeType.remove(0);

        showBar();

        whichTypes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String [] types = getResources().getStringArray(R.array.type_options);
            types[0] = "Fuel";

            boolean[] checkedTypes = new boolean[]{
                    true, false, false, false, false, false, false
            };
            final List<String> typesList = Arrays.asList(types);
            for (String alreadySet : excludeType) {
                checkedTypes[typesList.indexOf(alreadySet)] = false;
            }
            for (String st : types) {
                if (!excludeType.contains(st))
                    checkedTypes[typesList.indexOf(st)] = true;
            }
            excludeType = new ArrayList<>();
            builder.setMultiChoiceItems(types, checkedTypes, (dialog, which, isChecked) -> checkedTypes[which] = isChecked);
            builder.setTitle("Which types to include in barChart?");
            builder.setPositiveButton("CONFIRM", (dialog, which) -> {
                for (int i = 0; i < checkedTypes.length; i++)
                    if (!checkedTypes[i])
                        excludeType.add(typesList.get(i));
                    else
                        excludeType.remove(typesList.get(i));
                showBar();
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return view;
    }

    private void setUpBar() {
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.marker_template, creteXLabels(), "€");
        // Set the marker to the chart
        mv.setChartView(barChart);
        barChart.setMarker(mv);

        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);

        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);

        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        //xAxis.setLabelCount(7);
        //Instead of legend
        List<String> dates = creteXLabels();
        xAxis.setValueFormatter((value, axis) -> dates.get((int) value));
        xAxis.setTextSize(12f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(new MyValueAxisFormatter("€"));
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(new MyValueAxisFormatter("€"));
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        barChart.getLegend().setEnabled(false);
        barChart.animateY(750);
        barChart.setExtraOffsets(0, 0, 0, 5);
    }

    private List<BarEntry> createBarDataSet() {
        Cursor c;
        c = dbHelper.getAllDrives(vehicleID);

        String[] keys = getResources().getStringArray(R.array.type_options);
        keys[0] = "Fuel";

        Map<String, Double> costs = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM YY");
        Calendar calendar = Calendar.getInstance();
        try {
            while (c.moveToNext()) {
                long date = c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE));
                calendar.setTimeInMillis(date*1000);
                String monthYear = sdf.format(calendar.getTime());
                double price = Utils.calculateFullPrice(
                        c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE)),
                        c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES))
                );
                double old = 0.0;
                if (costs.containsKey(monthYear)) {
                    old = costs.get(monthYear);
                }
                costs.put(monthYear, old + price);
            }
        } catch (Exception ignored) {}

        for (String type : keys) {
            if (!type.equals("Fuel")) {
                if (!excludeType.contains(type)) {
                    c = dbHelper.getAllActualCostsFromType(vehicleID, type);
                    try {
                        while (c.moveToNext()) {
                            long date = c.getLong(c.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE));
                            calendar.setTimeInMillis(date*1000);
                            String monthYear = sdf.format(calendar.getTime());
                            double price = c.getDouble(c.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_EXPENSE));
                            double old = 0.0;
                            if (costs.containsKey(monthYear)) {
                                old = costs.get(monthYear);
                            }
                            costs.put(monthYear, old + price);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();

        float counter = 0f;
        for (double value : costs.values()) {
            if (Double.compare(value, 0.0) > 0)
                entries.add(new BarEntry(counter, (float) value));
            else
                counter += 1f;
            counter += 1f;
        }
        c.close();
        return entries;
    }

    public List<String> creteXLabels() {
        List<String> xLabels = new ArrayList<>();
        Long epochSecMin;
        if (dbHelper.getFirstCost(vehicleID) > dbHelper.getFirstDrive(vehicleID))
            epochSecMin = dbHelper.getFirstDrive(vehicleID);
        else
            epochSecMin = dbHelper.getFirstCost(vehicleID);
        Long epochSecMax;
        if (dbHelper.getLastCost(vehicleID) > dbHelper.getLastDrive(vehicleID))
            epochSecMax = dbHelper.getLastCost(vehicleID);
        else
            epochSecMax = dbHelper.getLastDrive(vehicleID);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM YY");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochSecMin*1000);
        String tmpLabel;
        do {
            tmpLabel = sdf.format(calendar.getTime());
            xLabels.add(tmpLabel);
            calendar.add(Calendar.MONTH, 1);
        } while (!tmpLabel.equals(sdf.format(epochSecMax*1000)));
        return xLabels;
    }

    private void showBar() {
        if (dbHelper.getFirstCost(vehicleID) == null || dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }
        barChart.clear();
        setUpBar();
        BarDataSet dataSet = new BarDataSet(createBarDataSet(), "");
        dataSet.setColors(Utils.getColoursSet());
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        data.setValueFormatter(new MyValueFormatter("€"));

        barChart.setData(data);
        barChart.setVisibleXRangeMaximum(10); // allow 20 values to be displayed at once on the x-axis, not more
        barChart.moveViewToX(0);
        barChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("Entry selected", e.toString());
        Log.i("LOW HIGH", "low: " + barChart.getLowestVisibleX() + ", high: " + barChart.getHighestVisibleX());
        Log.i("MIN MAX", "xMin: " + barChart.getXChartMin() + ", xMax: " + barChart.getXChartMax() + ", yMin: " + barChart.getYChartMin() + ", yMax: " + barChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}

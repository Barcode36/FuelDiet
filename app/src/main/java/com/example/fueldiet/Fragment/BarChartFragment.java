package com.example.fueldiet.Fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.MonthAxisValueFormatter;
import com.example.fueldiet.MyValueFormatter;
import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.IValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarChartFragment extends Fragment {

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

        excludeType = new ArrayList<>();
        excludeType.addAll(Arrays.asList(getResources().getStringArray(R.array.type_options)));
        excludeType.remove(0);
        showPie();

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
                showPie();
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return view;
    }

    private void setUpPie() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);

        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);

        barChart.setDrawGridBackground(false);

        //IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter(barChart);
        //lineDataSet.setValueFormatter(new Mont....)

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        //xAxis.setValueFormatter(xAxisFormatter);

        //IAxisValueFormatter custom = new MyValueFormatter("$");

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        //leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
    }

    private List<PieEntry> createPieDataSet() {
        Cursor c;/*
        c = dbHelper.getAllCostsWhereTimeBetween(vehicleID, epochs[0], epochs[1]);

        String[] keys = getResources().getStringArray(R.array.type_options);
        keys[0] = "Fuel";
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
        } catch (Exception ignored) {}*/

        List<PieEntry> entries = new ArrayList<>();
/*
        if (!excludeType.contains("Fuel")) {
            c = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, epochs[0], epochs[1]);
            try {
                while (c.moveToNext()) {
                    String tmp = "Fuel";
                    double pricePerLitre = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE));
                    double litres = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES));
                    double price = Utils.calculateFullPrice(pricePerLitre, litres);
                    Double value = costs.get(tmp);
                    value += price;
                    costs.put(tmp, value);
                }
            } catch (Exception ignored) {}
        }

        c.close();

        for (String key : costs.keySet())
            if (Double.compare(costs.get(key), 0.0) > 0)
                if (!excludeType.contains(key))
                    entries.add(new PieEntry(costs.get(key).floatValue(), key));
*/
        return entries;
    }

    private void showPie() {
        if (dbHelper.getFirstCost(vehicleID) == null || dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }
        barChart.clear();
        setUpPie();
        PieDataSet dataSet = new PieDataSet(createPieDataSet(), "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setDrawIcons(false);
        //dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        //dataSet.setColors(getColoursSet());
        PieData data = new PieData(dataSet);
        data.setValueTextSize(20f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PercentFormatter());

        //barChart.setData(data);
        barChart.invalidate();
    }
}

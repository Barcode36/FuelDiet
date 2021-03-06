package com.fueldiet.fueldiet.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.MyMarkerView;
import com.fueldiet.fueldiet.MyValueFormatter;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

public class BarChartFragment extends Fragment implements OnChartValueSelectedListener {

    private static final String TAG = "BarChartFragment";

    public BarChartFragment() { }

    private long vehicleID;
    private String vehicleInfo;
    private FuelDietDBHelper dbHelper;
    private BarChart barChart;
    private List<String> excludeType;

    public static BarChartFragment newInstance(long id, VehicleObject vo) {
        BarChartFragment fragment = new BarChartFragment();
        Bundle args = new Bundle();
        args.putLong("vehicleID", id);
        args.putString("vehicle_info", vo.getMake() + " " + vo.getModel());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleID = getArguments().getLong("vehicleID");
            vehicleInfo = getArguments().getString("vehicle_info");
        }
        dbHelper = FuelDietDBHelper.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);

        FloatingActionButton whichTypes = view.findViewById(R.id.vehicle_chart_bar_select_types);
        barChart = view.findViewById(R.id.vehicle_chart_bar);
        barChart.setNoDataText(getString(R.string.no_data_chart));
        barChart.setNoDataTextColor(R.color.primaryTextColor);
        barChart.setOnChartValueSelectedListener(this);

        if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null)
            whichTypes.setEnabled(false);

        excludeType = new ArrayList<>();
        excludeType.addAll(Arrays.asList(getResources().getStringArray(R.array.type_options)));

        showBar();

        FloatingActionButton saveChart = view.findViewById(R.id.vehicle_chart_save_img);
        saveChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barChart.saveToGallery("Bar_Chart_"+vehicleInfo+".jpg");
            }
        });

        whichTypes.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
            String [] types = Stream.concat(Arrays.stream(new String[]{getString(R.string.fuel)}), Arrays.stream(getResources().getStringArray(R.array.type_options))).toArray(String[]::new);

            boolean[] checkedTypes = new boolean[]{
                    true, false, false, false, false, false, false, false
            };
            final List<String> typesList = Arrays.asList(types);
            for (String alreadySet : excludeType) {
                checkedTypes[typesList.indexOf(alreadySet)] = false;
            }
            for (String st : types) {
                if (!excludeType.contains(st))
                    checkedTypes[typesList.indexOf(st)] = true;
            }
            builder.setMultiChoiceItems(types, checkedTypes, (dialog, which, isChecked) -> checkedTypes[which] = isChecked);
            builder.setTitle(getString(R.string.types_to_include));
            builder.setPositiveButton(getString(R.string.confirm).toUpperCase(), (dialog, which) -> {
                excludeType = new ArrayList<>();
                for (int i = 0; i < checkedTypes.length; i++)
                    if (!checkedTypes[i])
                        excludeType.add(typesList.get(i));
                    else
                        excludeType.remove(typesList.get(i));
                showBar();
            });
            builder.setNegativeButton(getString(R.string.cancel).toUpperCase(), (dialog, which) -> { });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return view;
    }

    private void setUpBar() {
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.marker_template, creteXLabels(), "???");
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
        xAxis.setGranularity(1f); // only intervals of 1 month
        xAxis.setGranularityEnabled(true);
        //Instead of legend

        List<String> dates = creteXLabels();
        xAxis.setValueFormatter(new MyValueFormatter(dates));
        xAxis.setTextSize(12f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(new MyValueFormatter("???"));
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        //leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
        /*
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(new MyValueFormatter("???"));
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)*/

        barChart.getXAxis().setLabelCount(dates.size());

        barChart.getLegend().setEnabled(false);
        barChart.animateY(750);
        barChart.setExtraOffsets(0, 0, 0, 5);
    }

    private List<BarEntry> createBarDataSet() {
        List<DriveObject> drives = dbHelper.getAllDrives(vehicleID);

        String[] keys = Stream.concat(Arrays.stream(new String[]{getString(R.string.fuel)}), Arrays.stream(getResources().getStringArray(R.array.type_options))).toArray(String[]::new);

        List<Double> costs = new ArrayList<>();
        List<String> labels = creteXLabels();
        for (int i = 0; i < labels.size(); i++) {
            costs.add(0.0);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM YY");
        Calendar calendar;
        if (!excludeType.contains(getString(R.string.fuel))) {
            for (DriveObject drive : drives) {
                calendar = drive.getDate();
                String monthYear = sdf.format(calendar.getTime());
                double price = Utils.calculateFullPrice(
                        drive.getCostPerLitre(), drive.getLitres()
                );
                int position = labels.indexOf(monthYear);
                double old = costs.get(position);
                costs.set(position, old - price);
            }
        }
        for (String type : keys) {
            if (!type.equals(getString(R.string.fuel))) {
                if (!excludeType.contains(type)) {
                    String newType = Utils.fromSLOtoENG(type);
                    List<CostObject> costObjects = dbHelper.getAllActualCostsFromType(vehicleID, newType);
                    for(CostObject co : costObjects) {
                        calendar = co.getDate();
                        String monthYear = sdf.format(calendar.getTime());
                        double price;
                        if (co.getCost() + 80085 == 0)
                            price = 0;
                        else
                            price = co.getCost();
                        int position = labels.indexOf(monthYear);
                        double old = costs.get(position);
                        costs.set(position, old - price);
                    }
                }
            }
        }
        List<BarEntry> entries = new ArrayList<>();

        float i = 0f;
        double minCost = 0.0;
        double maxCost = 0.0;
        for (double value : costs) {
            minCost = Math.min(minCost, value);
            maxCost = Math.max(maxCost, value);
            entries.add(new BarEntry(i, (float) value));
            i += 1f;
        }
        //make chart 10% bigger than necessary
        minCost *= 1.1;
        maxCost *= 1.1;
        barChart.getAxisLeft().setAxisMinimum((float) minCost);
        barChart.getAxisLeft().setAxisMaximum((float) maxCost);
        barChart.getAxisLeft().setInverted(true);
        return entries;
    }

    public List<String> creteXLabels() {
        List<String> xLabels = new ArrayList<>();
        Long epochSecMin;
        if (dbHelper.getFirstDrive(vehicleID) == null)
            epochSecMin = dbHelper.getFirstCost(vehicleID).getDateEpoch();
        else if (dbHelper.getFirstCost(vehicleID) == null)
            epochSecMin = dbHelper.getFirstDrive(vehicleID).getDateEpoch();
        else if (dbHelper.getFirstCost(vehicleID).getDate().after(dbHelper.getFirstDrive(vehicleID).getDate()))
            epochSecMin = dbHelper.getFirstDrive(vehicleID).getDateEpoch();
        else
            epochSecMin = dbHelper.getFirstCost(vehicleID).getDateEpoch();

        Long epochSecMax;
        if (dbHelper.getLastDrive(vehicleID) == null)
            epochSecMax = dbHelper.getLastCost(vehicleID).getDateEpoch();
        else if (dbHelper.getLastCost(vehicleID) == null)
            epochSecMax = dbHelper.getLastDrive(vehicleID).getDateEpoch();
        else if (dbHelper.getLastCost(vehicleID).getDate().after(dbHelper.getLastDrive(vehicleID).getDate()))
            epochSecMax = dbHelper.getLastCost(vehicleID).getDateEpoch();
        else
            epochSecMax = dbHelper.getLastDrive(vehicleID).getDateEpoch();

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
        if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }
        barChart.clear();
        setUpBar();
        List<BarEntry> entries = createBarDataSet();
        if (entries.size() == 0)
            return;
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(Utils.getColoursSet());
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(12f);
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);
        data.setValueFormatter(new MyValueFormatter("???"));

        barChart.setData(data);
        barChart.invalidate();
        barChart.setVisibleXRangeMaximum(9); // allow 10 values to be displayed at once on the x-axis, not more
        barChart.moveViewToX(-1);

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

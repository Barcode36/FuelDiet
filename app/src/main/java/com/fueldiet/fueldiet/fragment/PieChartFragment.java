package com.fueldiet.fueldiet.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.MyMarkerView;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartFragment extends Fragment implements NumberPicker.OnValueChangeListener, OnChartValueSelectedListener {

    private static final String TAG = "PieChartFragment";

    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private TextInputLayout fromDate;
    private TextInputLayout toDate;
    private PieChart pieChart;
    private String which;
    private List<String> excludeType;
    
    private Calendar smallEpoch;
    private Calendar smallestEpoch;
    private Calendar bigEpoch;
    private Calendar biggestEpoch;

    private SimpleDateFormat sdfDate = new SimpleDateFormat("MM. yyyy");

    public PieChartFragment() {
    }

    public static PieChartFragment newInstance(long id) {
        PieChartFragment fragment = new PieChartFragment();
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

        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);

        fromDate = view.findViewById(R.id.vehicle_chart_from_date);
        toDate = view.findViewById(R.id.vehicle_chart_to_date);
        Button whichTypes = view.findViewById(R.id.vehicle_chart_select_types);
        pieChart = view.findViewById(R.id.vehicle_chart_pie);
        pieChart.setNoDataText("Please ensure you have fuel and other costs");
        pieChart.setNoDataTextColor(R.color.primaryTextColor);

        if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null)
            whichTypes.setEnabled(false);

        setUpTimePeriod();
        excludeType = new ArrayList<>();
        excludeType.add(getString(R.string.fuel));
        excludeType.add(getString(R.string.other));
        showPie();

        fromDate.getEditText().setOnClickListener(v -> {
            if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null)
                return;
            which = "from";
            int[] dt = getMYfromDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getActivity().getSupportFragmentManager(), "time picker");
        });
        toDate.getEditText().setOnClickListener(v -> {
            if (dbHelper.getFirstCost(vehicleID) == null  && dbHelper.getFirstDrive(vehicleID) == null)
                return;
            which = "to";
            int[] dt = getMYtoDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getActivity().getSupportFragmentManager(), "time picker");
        });

        whichTypes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String [] types = getResources().getStringArray(R.array.type_options);
            types[0] = getString(R.string.fuel);

            boolean[] checkedTypes = new boolean[]{
                    false, true, true, true, true, true, false, false
            };
            final List<String> typesList = Arrays.asList(types);
            for (String alreadySet : excludeType) {
                checkedTypes[typesList.indexOf(alreadySet)] = false;
            }
            if (!excludeType.contains(getString(R.string.fuel)))
                checkedTypes[typesList.indexOf(getString(R.string.fuel))] = true;
            if (!excludeType.contains(types[types.length-1]))
                checkedTypes[typesList.indexOf(types[types.length-1])] = true;
            builder.setMultiChoiceItems(types, checkedTypes, (dialog, which, isChecked) -> checkedTypes[which] = isChecked);
            builder.setTitle("Which types to include in chart?");
            builder.setPositiveButton("CONFIRM", (dialog, which) -> {
                excludeType = new ArrayList<>();
                for (int i = 0; i < checkedTypes.length; i++)
                    if (!checkedTypes[i])
                        excludeType.add(typesList.get(i));
                    else
                        excludeType.remove(typesList.get(i));
                showPie();
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> {
                smallEpoch = null;
                bigEpoch = null;
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        return view;
    }

    private int[] getMYfromDate() {
        int [] my = new int[2];
        my[0] = Integer.parseInt(fromDate.getEditText().getText().toString().split("\\.")[0].trim());
        my[1] = Integer.parseInt(fromDate.getEditText().getText().toString().split("\\.")[1].trim());
        return my;
    }
    private int[] getMYtoDate() {
        int [] my = new int[2];
        my[0] = Integer.parseInt(toDate.getEditText().getText().toString().split("\\.")[0].trim());
        my[1] = Integer.parseInt(toDate.getEditText().getText().toString().split("\\.")[1].trim());
        return my;
    }

    private void setUpTimePeriod() {

        if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }

        Long epochSecMin;
        if (dbHelper.getFirstDrive(vehicleID) == null)
            epochSecMin = dbHelper.getFirstCost(vehicleID).getDateEpoch();
        else if (dbHelper.getFirstCost(vehicleID) == null || dbHelper.getFirstCost(vehicleID).getDate().after(dbHelper.getFirstDrive(vehicleID).getDate()))
            epochSecMin = dbHelper.getFirstDrive(vehicleID).getDateEpoch();
        else
            epochSecMin = dbHelper.getFirstCost(vehicleID).getDateEpoch();

        smallestEpoch = Calendar.getInstance();
        smallestEpoch.setTimeInMillis(epochSecMin*1000);
        smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
        smallestEpoch.set(Calendar.HOUR, 1);
        smallestEpoch.set(Calendar.MINUTE, 1);

        Long epochSecMax;
        if (dbHelper.getLastDrive(vehicleID) == null)
            epochSecMax = dbHelper.getLastCost(vehicleID).getDateEpoch();
        else if (dbHelper.getLastCost(vehicleID) == null || dbHelper.getLastCost(vehicleID).getDate().before(dbHelper.getLastDrive(vehicleID).getDate()))
            epochSecMax = dbHelper.getLastDrive(vehicleID).getDateEpoch();
        else
            epochSecMax = dbHelper.getLastCost(vehicleID).getDateEpoch();

        biggestEpoch = Calendar.getInstance();
        biggestEpoch.setTimeInMillis(epochSecMax*1000);
        biggestEpoch.set(Calendar.DAY_OF_MONTH, biggestEpoch.getActualMaximum(Calendar.DAY_OF_MONTH));
        biggestEpoch.set(Calendar.HOUR_OF_DAY, 23);
        biggestEpoch.set(Calendar.MINUTE, 55);
        
        if (smallEpoch == null && bigEpoch == null) {
            fromDate.getEditText().setText(sdfDate.format(smallestEpoch.getTime()));
            toDate.getEditText().setText(sdfDate.format(biggestEpoch.getTime()));
        } else if (smallEpoch == null){
            fromDate.getEditText().setText(sdfDate.format(smallestEpoch.getTime()));
            toDate.getEditText().setText(sdfDate.format(bigEpoch.getTime()));
        } else {
            fromDate.getEditText().setText(sdfDate.format(smallEpoch.getTime()));
            toDate.getEditText().setText(sdfDate.format(biggestEpoch.getTime()));
        }
    }

    private void setUpPie() {
        MyMarkerView mv = new MyMarkerView(getActivity(), R.layout.marker_template, null, "€");
        // Set the marker to the chart
        mv.setChartView(pieChart);
        pieChart.setMarker(mv);
        /*
        Legend
         */
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextSize(15f);
        l.setFormSize(15f);
        l.setWordWrapEnabled(true);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        //Loading animation
        pieChart.animateXY(500, 500);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        //Replaced with legend
        pieChart.setDrawEntryLabels(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawHoleEnabled(false);
        //pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(110);
        //pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.highlightValues(null);
    }

    private long[] getBothEpoch() {
        if (smallEpoch == null && bigEpoch == null)
            return new long[]{
                    (smallestEpoch.getTimeInMillis()/1000),
                    (biggestEpoch.getTimeInMillis()/1000)
            };
        else if (smallEpoch == null)
            return new long[]{
                    (smallestEpoch.getTimeInMillis()/1000),
                    (bigEpoch.getTimeInMillis()/1000)
            };
        else if (bigEpoch == null)
            return new long[]{
                    (smallEpoch.getTimeInMillis()/1000),
                    (biggestEpoch.getTimeInMillis()/1000)
            };
        else
            return new long[]{
                    (smallEpoch.getTimeInMillis()/1000),
                    (bigEpoch.getTimeInMillis()/1000)
            };
    }

    private List<PieEntry> createPieDataSet() {
        long[] epochs = getBothEpoch();
        List<CostObject> costObjects = dbHelper.getAllCostsWhereTimeBetween(vehicleID, epochs[0], epochs[1]);
        String[] keys = getResources().getStringArray(R.array.type_options);
        //keys[0] = getString(R.string.fuel);
        Map<String, Double> costs = new HashMap<>();
        costs.put(getString(R.string.fuel), 0.0);
        for (String key : keys)
            costs.put(key, 0.0);

        for(CostObject co : costObjects) {
            String tmp = co.getType();
            if (keys[0].equals("Gorivo"))
                tmp = Utils.fromENGtoSLO(tmp);
            double tmpPrice/* = Math.max(co.getCost(), 0)*/;
            if (co.getCost() + 80085 == 0)
                tmpPrice = 0;
            else
                tmpPrice = co.getCost();
            Double value = costs.get(tmp);
            value += tmpPrice;
            costs.put(tmp, value);
        }

        List<PieEntry> entries = new ArrayList<>();

        if (!excludeType.contains(getString(R.string.fuel))) {
            List<DriveObject> drives = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, epochs[0], epochs[1]);
                for(DriveObject drive : drives) {
                    String tmp = getString(R.string.fuel);
                    double pricePerLitre = drive.getCostPerLitre();
                    double litres = drive.getLitres();
                    double price = Utils.calculateFullPrice(pricePerLitre, litres);
                    Double value = costs.get(tmp);
                    value += price;
                    costs.put(tmp, value);
                }
        }

        for (String key : costs.keySet())
            if (Double.compare(costs.get(key), 0.0) > 0)
                if (!excludeType.contains(key))
                    entries.add(new PieEntry(costs.get(key).floatValue(), key));

        return entries;
    }

    private void showPie() {
        if (dbHelper.getFirstCost(vehicleID) == null && dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }
        pieChart.clear();
        setUpPie();
        List<PieEntry> pieData = createPieDataSet();
        PieDataSet dataSet = new PieDataSet(pieData, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(20f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueFormatter(new MyValueFormatter("%"));
        data.setValueFormatter(new PercentFormatter(pieChart));

        pieChart.setData(data);
        pieChart.invalidate();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //picker is NULL!!!!, oldVal is month, newVal is year
        Calendar calendar = Calendar.getInstance();

        if (which.equals("from")) {
            calendar.set(newVal, oldVal-1, 1, 1,1);
            smallEpoch = calendar;
            if (bigEpoch == null && calendar.after(biggestEpoch)) {
                Toast.makeText(getContext(), "From date cannot be after To date", Toast.LENGTH_SHORT).show();
                smallEpoch = null;
                which = null;
                return;
            } else if (bigEpoch != null && calendar.after(bigEpoch)) {
                Toast.makeText(getContext(), "From date cannot be after To date", Toast.LENGTH_SHORT).show();
                smallEpoch = null;
                which = null;
                return;
            }
            fromDate.getEditText().setText(sdfDate.format(smallEpoch.getTime()));
        } else {
            calendar.set(newVal, oldVal-1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23,59);
            bigEpoch = calendar;
            if (smallEpoch == null && calendar.before(smallestEpoch)) {
                Toast.makeText(getContext(), "To date cannot be before From date", Toast.LENGTH_SHORT).show();
                bigEpoch = null;
                which = null;
                return;
            } else if (smallEpoch != null && calendar.before(smallEpoch)) {
                Toast.makeText(getContext(), "To date cannot be before From date", Toast.LENGTH_SHORT).show();
                bigEpoch = null;
                which = null;
                return;
            }
            toDate.getEditText().setText(sdfDate.format(bigEpoch.getTime()));
        }
        which = null;
        showPie();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}

package com.example.fueldiet.Fragment;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartFragment extends Fragment implements NumberPicker.OnValueChangeListener {

    private long vehicleID;
    private FuelDietDBHelper dbHelper;
    private View view;
    
    private TextInputLayout fromDate;
    private TextInputLayout toDate;
    private Button whichTypes;
    private Button showChart;
    private PieChart pieChart;
    private String which;
    private List<String> excludeType;
    
    private Calendar smallEpoch;
    private Calendar smallestEpoch;
    private Calendar bigEpoch;
    private Calendar biggestEpoch;

    SimpleDateFormat sdfDate = new SimpleDateFormat("MM. yyyy");

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

        view = inflater.inflate(R.layout.fragment_pie_chart, container, false);

        fromDate = view.findViewById(R.id.vehicle_chart_from_date);
        toDate = view.findViewById(R.id.vehicle_chart_to_date);
        whichTypes = view.findViewById(R.id.vehicle_chart_select_types);
        showChart = view.findViewById(R.id.vehicle_chart_show);
        pieChart = view.findViewById(R.id.vehicle_chart_pie);
        pieChart.setNoDataText("PieChart is waiting...");

        //setUpPie();
        setUpTimePeriod();
        excludeType = new ArrayList<>();
        excludeType.add("Fuel");
        showPie();

        fromDate.getEditText().setOnClickListener(v -> {
            which = "from";
            int[] dt = getMYfromDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getActivity().getSupportFragmentManager(), "time picker");
        });
        toDate.getEditText().setOnClickListener(v -> {
            which = "to";
            int[] dt = getMYtoDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getActivity().getSupportFragmentManager(), "time picker");
        });

        showChart.setOnClickListener(v -> {
            //if ()
        });

        whichTypes.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String [] types = getResources().getStringArray(R.array.type_options);
            types[0] = "Fuel";
            /*
            String [] types = new String[7];
            for (int z = 1; z < tmpTypes.length; z++)
                types[z-1] = tmpTypes[z];

             */
            boolean[] checkedTypes = new boolean[]{
                    false, true, true, true, true, true, true
            };
            final List<String> typesList = Arrays.asList(types);
            for (String alreadySet : excludeType) {
                checkedTypes[typesList.indexOf(alreadySet)] = false;
            }
            if (!excludeType.contains("Fuel"))
                checkedTypes[typesList.indexOf("Fuel")] = true;
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
                        else
                            excludeType.remove(typesList.get(i));
                    showPie();
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
        
        String epochSecMin = dbHelper.getFirstCost(vehicleID);
        smallestEpoch = Calendar.getInstance();
        smallestEpoch.setTimeInMillis(Long.parseLong(epochSecMin)*1000);
        smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
        smallestEpoch.set(Calendar.HOUR, 1);
        smallestEpoch.set(Calendar.MINUTE, 1);

        String epochSecMax = dbHelper.getLastCost(vehicleID);
        biggestEpoch = Calendar.getInstance();
        biggestEpoch.setTimeInMillis(Long.parseLong(epochSecMax)*1000);
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
        Cursor c;
        long[] epochs = getBothEpoch();
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
        } catch (Exception e) {}

        List<PieEntry> entries = new ArrayList<>();

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
            } catch (Exception e) {}
        }

        c.close();

        for (String key : costs.keySet())
            if (Double.compare(costs.get(key), 0.0) > 0)
                if (!excludeType.contains(key))
                    entries.add(new PieEntry(costs.get(key).floatValue(), key));

        return entries;
    }

    private void showPie() {
        pieChart.clear();
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

        pieChart.setData(data);
        pieChart.invalidate();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        //picker is NULL!!!!, oldVal is month, newVal is year
        Calendar calendar = Calendar.getInstance();
        //calendar.set(newVal, oldVal-1, 1, 2,1);

        if (which.equals("from")) {
            calendar.set(newVal, oldVal-1, 1, 1,1);
            smallEpoch = calendar;
            fromDate.getEditText().setText(sdfDate.format(smallEpoch.getTime()));
        } else {
            calendar.set(newVal, oldVal-1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23,59);
            bigEpoch = calendar;
            toDate.getEditText().setText(sdfDate.format(bigEpoch.getTime()));
        }
        which = null;
        showPie();
    }
}

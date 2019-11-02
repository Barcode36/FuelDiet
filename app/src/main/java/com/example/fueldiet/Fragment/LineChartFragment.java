package com.example.fueldiet.Fragment;

import android.content.res.Resources;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineChartFragment extends Fragment implements NumberPicker.OnValueChangeListener {
    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    private TextInputLayout fromDate;
    private TextInputLayout toDate;
    private LineChart lineChart;
    private String which;

    private Calendar smallEpoch;
    private Calendar smallestEpoch;
    private Calendar bigEpoch;
    private Calendar biggestEpoch;

    private SimpleDateFormat sdfDate = new SimpleDateFormat("MM. yyyy");
    private SimpleDateFormat sdfLineDate = new SimpleDateFormat("dd.MM.yy");

    public LineChartFragment() {
    }

    public static LineChartFragment newInstance(long id) {
        LineChartFragment fragment = new LineChartFragment();
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

        View view = inflater.inflate(R.layout.fragment_line_chart, container, false);

        fromDate = view.findViewById(R.id.vehicle_chart_from_date);
        toDate = view.findViewById(R.id.vehicle_chart_to_date);
        lineChart = view.findViewById(R.id.vehicle_chart_line);
        lineChart.setNoDataText("LineChart is waiting...");

        setUpTimePeriod();
        showLine();

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

        if (dbHelper.getFirstDrive(vehicleID) == null) {
            return;
        }

        long epochSecMin = dbHelper.getFirstDrive(vehicleID);
        smallestEpoch = Calendar.getInstance();
        smallestEpoch.setTimeInMillis(epochSecMin*1000);
        smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
        smallestEpoch.set(Calendar.HOUR, 1);
        smallestEpoch.set(Calendar.MINUTE, 1);

        long epochSecMax = dbHelper.getLastDrive(vehicleID);
        biggestEpoch = Calendar.getInstance();
        biggestEpoch.setTimeInMillis(epochSecMax*1000);
        int z = biggestEpoch.getActualMaximum(Calendar.DAY_OF_MONTH);
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

    private void setUpLine() {
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setLabelRotationAngle(-60f);
        lineChart.getXAxis().setGranularityEnabled(true);

        lineChart.setTouchEnabled(true);
        // enable touch gestures
        lineChart.setTouchEnabled(true);
        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.getDescription().setText("Fuel consumption");
        lineChart.getDescription().setTextSize(16f);
        lineChart.getDescription().setEnabled(true);
        lineChart.getLegend().setEnabled(false);
        lineChart.animateY(1000);
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

    private List[] createLineDataSet() {
        List<Entry> consumptionValues = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        Cursor c;
        long[] epochs = getBothEpoch();
        c = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, epochs[0], epochs[1]);
        double minCons = 100000.0;
        double maxCons = -1000.0;
        int counter = 0;
        double sum = 0.0;
        try {
            while (c.moveToNext()) {
                String timedate = parseToDate(c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE)));
                int trip = c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM));
                double litres = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES));
                double cons = Utils.calculateConsumption(trip, litres);
                minCons = minCons < cons ? minCons : cons;
                maxCons = maxCons > cons ? maxCons : cons;
                consumptionValues.add(new Entry((float)counter, (float)cons));
                sum += cons;
                dates.add(timedate);
                counter++;
            }
        } finally {
            c.close();
        }
        double avg = sum / dates.size();
        double upperLimit = avg + ((maxCons - avg) / 2);
        double lowerLimit = avg - ((avg - minCons) / 2);
        List<Integer> colors = new ArrayList<>();
        for (Entry e : consumptionValues){
            double cons = e.getY();
            if (cons > upperLimit)
                //red
                colors.add(ColorTemplate.MATERIAL_COLORS[2]);
            else if (cons < lowerLimit)
                //green
                colors.add(ColorTemplate.MATERIAL_COLORS[0]);
            else if (cons >= avg)
                //yellow
                colors.add(ColorTemplate.MATERIAL_COLORS[1]);
            else
                //blue
                colors.add(ColorTemplate.MATERIAL_COLORS[3]);


        }
        return new List[]{consumptionValues, dates, colors};
    }

    private String parseToDate(long epoch) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(epoch*1000);
        return sdfLineDate.format(date.getTime());
    }

    private void showLine() {
        lineChart.clear();
        setUpLine();
        List[] dataList = createLineDataSet();
        List<Entry> consumptionValues = dataList[0];
        List<String> dates = dataList[1];

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        LineDataSet values = new LineDataSet(consumptionValues, "Consumption");
        //values.setAxisDependency(YAxis.AxisDependency.LEFT);
        values.setLineWidth(3.5f);
        values.setCircleHoleRadius(4.5f);
        values.setValueTextSize(12f);
        values.setCircleRadius(7.5f);
        values.setMode(values.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                ? LineDataSet.Mode.LINEAR
                :  LineDataSet.Mode.HORIZONTAL_BEZIER);

        //values.setDrawFilled(true);

        dataSets.add(values);

        ((LineDataSet) dataSets.get(0)).setColor(getContext().getColor(R.color.colorPrimary));
        ((LineDataSet) dataSets.get(0)).setCircleColors(dataList[2]);

        LineData data = new LineData(dataSets);
        //lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates.get((int) value);
            }
        });
        lineChart.setData(data);
        lineChart.getData().setHighlightEnabled(false);

        //lineChart.getXAxis().setLabelCount(dates.size());
        /*Scrolling and max 9 elements per view*/
        //lineChart.setVisibleXRangeMaximum(8f);
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
            fromDate.getEditText().setText(sdfDate.format(smallEpoch.getTime()));
        } else {
            calendar.set(newVal, oldVal-1, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23,59);
            bigEpoch = calendar;
            toDate.getEditText().setText(sdfDate.format(bigEpoch.getTime()));
        }
        which = null;
        showLine();
    }
}
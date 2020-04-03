package com.fueldiet.fueldiet.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.fueldiet.fueldiet.MyMarkerView;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LineChartFragment extends Fragment implements NumberPicker.OnValueChangeListener, OnChartValueSelectedListener {
    private static final String TAG = "LineChartFragment";
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

    private double minCons;
    private double maxCons;

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
        lineChart.setNoDataText("No fueling is logged. No data to show.");
        lineChart.setNoDataTextColor(R.color.primaryTextColor);
        lineChart.setOnChartValueSelectedListener(this);

        setUpTimePeriod();

        showLine();

        fromDate.getEditText().setOnClickListener(v -> {
            if (dbHelper.getFirstDrive(vehicleID) == null || dbHelper.getFirstDrive(vehicleID).getDateEpoch() == 0)
                return;
            which = "from";
            int[] dt = getMYfromDate();
            MonthYearPickerFragment newFragment = new MonthYearPickerFragment(dt[0], dt[1]);
            newFragment.setValueChangeListener(this);
            newFragment.show(getActivity().getSupportFragmentManager(), "time picker");
        });
        toDate.getEditText().setOnClickListener(v -> {
            if (dbHelper.getFirstDrive(vehicleID) == null || dbHelper.getFirstDrive(vehicleID).getDateEpoch() == 0)
                return;
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

        long epochSecMin = dbHelper.getFirstDrive(vehicleID).getDateEpoch();
        smallestEpoch = Calendar.getInstance();
        smallestEpoch.setTimeInMillis(epochSecMin*1000);
        smallestEpoch.set(Calendar.DAY_OF_MONTH, 1);
        smallestEpoch.set(Calendar.HOUR, 1);
        smallestEpoch.set(Calendar.MINUTE, 1);

        long epochSecMax = dbHelper.getLastDrive(vehicleID).getDateEpoch();
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
        lineChart.getDescription().setText(getString(R.string.fuel_consumption));
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
        long[] epochs = getBothEpoch();
        String consUnit = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("selected_unit", "litres_per_km");
        boolean l_p_km = consUnit.equals("litres_per_km");
        List<DriveObject> drives = dbHelper.getAllDrivesWhereTimeBetween(vehicleID, epochs[0], epochs[1]);
        minCons = 100000.0;
        maxCons = -1000.0;
        int counter = 0;
        double sum = 0.0;

        double sumL = 0.0;
        int sumK = 0;
        for (int u = 0; u < drives.size(); u++) {
            DriveObject drive = drives.get(u);
            //int trip = 0;
            //double litres = 0.0;
            if (drive.getFirst() == 0 && drive.getNotFull() == 1) {
                sumK += drive.getTrip();
                sumL += drive.getLitres();
            } else if (drive.getFirst() == 0 && drive.getNotFull() == 0) {
                String timedate = parseToDate(drive.getDateEpoch());
                sumK += drive.getTrip();
                sumL += drive.getLitres();
                double cons;
                if (l_p_km) {
                    cons = Utils.calculateConsumption(sumK, sumL);
                } else {
                    cons = Utils.convertUnitToKmPL(Utils.calculateConsumption(sumK, sumL));
                }
                minCons = Math.min(minCons, cons);
                maxCons = Math.max(maxCons, cons);
                consumptionValues.add(new Entry((float) counter, (float) cons));
                sum += cons;
                dates.add(timedate);
                counter++;
                sumL = 0.0;
                sumK = 0;
            }
        }


        double avg = sum / dates.size();
        double upperLimit = avg + ((maxCons - avg) / 2);
        double lowerLimit = avg - ((avg - minCons) / 2);
        List<Integer> colors = new ArrayList<>();
        for (Entry e : consumptionValues){
            double cons = e.getY();
            if (cons > upperLimit)
                if (l_p_km)
                    //red
                    colors.add(ColorTemplate.MATERIAL_COLORS[2]);
                else
                    //green
                    colors.add(ColorTemplate.MATERIAL_COLORS[0]);
            else if (cons < lowerLimit)
                if (l_p_km)
                    //green
                    colors.add(ColorTemplate.MATERIAL_COLORS[0]);
                else
                    //red
                    colors.add(ColorTemplate.MATERIAL_COLORS[2]);
            else if (cons >= avg)
                if (l_p_km)
                    //yellow
                    colors.add(ColorTemplate.MATERIAL_COLORS[1]);
                else
                    //blue
                    colors.add(ColorTemplate.MATERIAL_COLORS[3]);
            else
                if (l_p_km)
                    //blue
                    colors.add(ColorTemplate.MATERIAL_COLORS[3]);
                else
                    //yellow
                    colors.add(ColorTemplate.MATERIAL_COLORS[1]);
        }
        return new List[]{consumptionValues, dates, colors};
    }

    private String parseToDate(long epoch) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(epoch*1000);
        return sdfLineDate.format(date.getTime());
    }

    private void showLine() {
        if (dbHelper.getFirstDrive(vehicleID) == null || dbHelper.getFirstDrive(vehicleID).getDateEpoch() == 0)
            return;
        lineChart.clear();
        setUpLine();
        List[] dataList = createLineDataSet();
        List<Entry> consumptionValues = dataList[0];
        List<String> dates = dataList[1];
        String consUnit = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("selected_unit", "litres_per_km");
        MyMarkerView mv;
        if (consUnit.equals("litres_per_km")) {
            mv = new MyMarkerView(getActivity(), R.layout.marker_template, dates, "l/100km");
        } else {
            mv = new MyMarkerView(getActivity(), R.layout.marker_template, dates, "km/l");
        }


        // Set the marker to the chart
        mv.setChartView(lineChart);
        lineChart.setMarker(mv);

        List<ILineDataSet> dataSets = new ArrayList<>();
        LineDataSet values = new LineDataSet(consumptionValues, getString(R.string.tab_text_1));
        values.setLineWidth(3.5f);
        values.setCircleHoleRadius(4.5f);
        values.setDrawValues(false);
        values.setValueTextSize(12f);
        values.setCircleRadius(7.5f);
        values.setMode(values.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                ? LineDataSet.Mode.LINEAR
                :  LineDataSet.Mode.HORIZONTAL_BEZIER);

        dataSets.add(values);


        ((LineDataSet) dataSets.get(0)).setColor(getContext().getColor(R.color.colorPrimary));
        ((LineDataSet) dataSets.get(0)).setCircleColors(dataList[2]);
        ((LineDataSet) dataSets.get(0)).setDrawHighlightIndicators(false);

        LineData data = new LineData(dataSets);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        lineChart.setData(data);
        lineChart.setHighlightPerTapEnabled(true);

        lineChart.getAxisLeft().setLabelCount(6, true);

        if (consUnit.equals("litres_per_km")) {
            Log.d(TAG, "showLine: minimum " + minCons);
            Log.d(TAG, "showLine: maximum " + maxCons);
            lineChart.getAxisLeft().setAxisMinimum((float) (minCons - 0.1));
            lineChart.getAxisLeft().setAxisMaximum((float) (maxCons + 0.1));
        } else {
            Log.d(TAG, "showLine: minimum " + (float) (minCons - 1));
            Log.d(TAG, "showLine: maximum " + (float) (maxCons + 1));
            lineChart.getAxisLeft().setAxisMinimum((float) (minCons - 1));
            lineChart.getAxisLeft().setAxisMaximum((float) (maxCons + 1));
        }
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
        showLine();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.d(TAG, "onValueSelected: entry selected " + e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.d(TAG, "onNothingSelected");
    }
}
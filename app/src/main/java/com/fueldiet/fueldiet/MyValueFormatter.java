package com.fueldiet.fueldiet;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Custom Value formatter for MPAndroidChart
 */
public class MyValueFormatter extends ValueFormatter {

    private DecimalFormat format;
    private String suffix;
    private List<String> dates;

    public MyValueFormatter(String suffix) {
        format = new DecimalFormat("###,###,###,##0");
        this.suffix = suffix;
    }

    public MyValueFormatter(List<String> dates) {
        this.dates = dates;
    }

    @Override
    public String getPieLabel(float value, PieEntry pieEntry) {
        //return super.getPieLabel(value, pieEntry);
        return format.format(value) + suffix;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (dates != null)
            //barchart
            return dates.get((int)value);
        else
            //linechart
            return format.format(value) + suffix;
    }
}

package com.example.fueldiet;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Custom Value formatter for MPAndroidChart
 */
public class MyValueAxisFormatter implements IAxisValueFormatter {

    private DecimalFormat format;
    private String suffix;

    public MyValueAxisFormatter(String suffix) {
        format = new DecimalFormat("###,###,###,##0");
        this.suffix = suffix;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return format.format(value) + suffix;
    }
}

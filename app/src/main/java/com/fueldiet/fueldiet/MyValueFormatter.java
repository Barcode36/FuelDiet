package com.fueldiet.fueldiet;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Custom Value formatter for MPAndroidChart
 */
public class MyValueFormatter implements IValueFormatter {

    private DecimalFormat format;
    private String suffix;

    public MyValueFormatter(String suffix) {
        format = new DecimalFormat("###,###,###,##0");
        this.suffix = suffix;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return format.format(value) + suffix;
    }
}

package com.example.fueldiet;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MonthAxisValueFormatter implements IValueFormatter {

    private List<String> allMonths;
    private BarChart chart;

    public MonthAxisValueFormatter(BarChart chart) {
        this.chart = chart;
        String[] months = new DateFormatSymbols().getMonths();
        List<String> allMonths = new ArrayList<String>();
        for(String singleMonth: months){
            allMonths.add(singleMonth);
        }
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return null;
    }
}

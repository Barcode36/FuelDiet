package com.fueldiet.fueldiet.fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;


public class TimeDatePickerHelper {

    public static MaterialTimePicker createTime(Calendar calendar) {
        MaterialTimePicker.Builder materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE));
        return materialTimePicker.build();
    }

    public static MaterialDatePicker<?> createDate(Calendar calendar) {
        MaterialDatePicker.Builder<?> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setSelection(calendar.getTimeInMillis());
        return materialDatePicker.build();
    }
}

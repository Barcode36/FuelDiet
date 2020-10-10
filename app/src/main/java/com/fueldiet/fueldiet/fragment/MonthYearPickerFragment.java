package com.fueldiet.fueldiet.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Custom Dialog for Date Picker
 */
public class MonthYearPickerFragment extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;

    private int M;
    private int Y;

    public MonthYearPickerFragment(int m, int y) {
        this.M = m;
        this.Y = y;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_monthyear_dialog, null);
        builder.setTitle("Choose a date");

        final NumberPicker numberPickerY = view.findViewById(R.id.year_dialog_layout);
        final NumberPicker numberPickerM = view.findViewById(R.id.month_dialog_layout);

        numberPickerM.setMinValue(1);
        numberPickerM.setMaxValue(12);
        numberPickerY.setMinValue(2018);
        numberPickerY.setMaxValue(2030);

        numberPickerM.setValue(M);
        numberPickerY.setValue(Y);

        builder.setView(view);

        builder.setPositiveButton(getString(R.string.ok).toUpperCase(), (dialog, which) -> {
            int m = numberPickerM.getValue();
            int y = numberPickerY.getValue();
            valueChangeListener.onValueChange(null, m, y);
        });

        builder.setNegativeButton(getString(R.string.cancel).toUpperCase(), (dialog, which) -> {
            //int m = numberPickerM.getValue();
            //int y = numberPickerY.getValue();
            //valueChangeListener.onValueChange(null, m, y);
        });
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}

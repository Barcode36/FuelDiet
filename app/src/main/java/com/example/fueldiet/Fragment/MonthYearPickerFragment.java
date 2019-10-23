package com.example.fueldiet.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.example.fueldiet.R;

public class MonthYearPickerFragment extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.monthyear_dialog_fragment, null);
        builder.setTitle("Choose a date");

        final NumberPicker numberPickerY = view.findViewById(R.id.year_dialog_layout);
        final NumberPicker numberPickerM = view.findViewById(R.id.month_dialog_layout);

        numberPickerM.setMinValue(1);
        numberPickerM.setMaxValue(12);
        numberPickerY.setMinValue(2018);
        numberPickerY.setMaxValue(2030);

        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int m = numberPickerM.getValue();
            int y = numberPickerY.getValue();
            valueChangeListener.onValueChange(numberPickerM,
                    numberPickerM.getValue(), numberPickerM.getValue());
            valueChangeListener.onValueChange(numberPickerY,
                    numberPickerY.getValue(), numberPickerY.getValue());
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            valueChangeListener.onValueChange(numberPickerM,
                    numberPickerM.getValue(), numberPickerM.getValue());
            valueChangeListener.onValueChange(numberPickerY,
                    numberPickerY.getValue(), numberPickerY.getValue());
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

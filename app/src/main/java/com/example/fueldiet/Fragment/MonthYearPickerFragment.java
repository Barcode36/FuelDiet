package com.example.fueldiet.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;
import com.example.fueldiet.R;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_monthyear_dialog, null);
        builder.setTitle("Choose a date");

        final NumberPicker numberPickerY = view.findViewById(R.id.year_dialog_layout);
        final NumberPicker numberPickerM = view.findViewById(R.id.month_dialog_layout);

        numberPickerM.setMinValue(1);
        numberPickerM.setMaxValue(12);
        numberPickerY.setMinValue(2018);
        numberPickerY.setMaxValue(2030);

        /*
        Calendar c = Calendar.getInstance();
        numberPickerM.setValue(c.get(Calendar.MONTH)+1);
        numberPickerY.setValue(c.get(Calendar.YEAR));
         */
        numberPickerM.setValue(M);
        numberPickerY.setValue(Y);

        builder.setView(view);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int m = numberPickerM.getValue();
            int y = numberPickerY.getValue();
            valueChangeListener.onValueChange(null, m, y);
        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            int m = numberPickerM.getValue();
            int y = numberPickerY.getValue();
            valueChangeListener.onValueChange(null, m, y);
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

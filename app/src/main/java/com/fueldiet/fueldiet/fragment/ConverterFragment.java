package com.fueldiet.fueldiet.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fueldiet.fueldiet.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ConverterFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "ConverterFragment";

    TextInputLayout originalValue;
    TextInputLayout convertedValue;

    Spinner fromUnit;
    Spinner toUnit;

    TextWatcher forOriginal;
    TextWatcher forConverted;

    String recent = "";
    Units to;
    Units from;

    public static ConverterFragment newInstance() {
        return new ConverterFragment();
    }

    private enum Units {
        KM, M, YARDS, L, GL_US, GL_UK, MPG_US, MPG_UK, L_100_KM, KM_L
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_converter, container, false);

        originalValue = view.findViewById(R.id.converter_input_value);
        convertedValue = view.findViewById(R.id.converter_result_input);
        fromUnit = view.findViewById(R.id.converter_from);
        toUnit = view.findViewById(R.id.converter_to);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()), R.array.units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromUnit.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()), R.array.units, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toUnit.setAdapter(adapter1);
        toUnit.setSelection(1);

        fromUnit.setOnItemSelectedListener(this);
        toUnit.setOnItemSelectedListener(this);

        createTextWatchers();

        return view;
    }

    private void createTextWatchers() {
        forOriginal = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /*
                if (!litre.getEditText().getText().toString().equals("") && !recent.equals("litre") && s.length() > 0) {
                    Double cons = Utils.calculateConsumption(Integer.parseInt(km.getEditText().getText().toString()), Double.parseDouble(litre.getEditText().getText().toString()));
                    removeTextWatcherConsumption();
                    consumption.getEditText().setText(cons + "");
                    recent = "consumption";
                    addTextWatcherConsumption();
                } else if (!consumption.getEditText().getText().toString().equals("") && !recent.equals("consumption") && s.length() > 0) {
                    BigDecimal cons = new BigDecimal(consumption.getEditText().getText().toString());
                    cons = cons.divide(BigDecimal.valueOf(100));
                    cons = cons.multiply(new BigDecimal(s.toString()));
                    cons = cons.setScale(2, RoundingMode.HALF_UP);
                    removeTextWatcherLitre();
                    litre.getEditText().setText(cons.toString());
                    recent = "litre";
                    addTextWatcherLitre();

                    if (!litrePrice.getEditText().getText().toString().equals("")) {
                        removeTextWatcherPrice();
                        Double fullPrice = Utils.calculateFullPrice(Double.parseDouble(litrePrice.getEditText().getText().toString()), Double.parseDouble(cons.toString()));
                        price.getEditText().setText(fullPrice + "");
                        addTextWatcherPrice();
                    } else if (!price.getEditText().getText().toString().equals("")) {
                        removeTextWatcherLitrePrice();
                        Double lPrice = Utils.calculateLitrePrice(Double.parseDouble(price.getEditText().getText().toString()), Double.parseDouble(cons.toString()));
                        litrePrice.getEditText().setText(lPrice + "");
                        addTextWatcherLitrePrice();
                    }
                }*/
            }
        };
        forConverted = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /*
                if (!km.getEditText().getText().toString().equals("") && !recent.equals("km") && s.length() > 0) {
                    BigDecimal cons = new BigDecimal(consumption.getEditText().getText().toString());
                    cons = cons.divide(BigDecimal.valueOf(100));
                    cons = cons.multiply(new BigDecimal(km.getEditText().getText().toString()));
                    cons = cons.setScale(2, RoundingMode.HALF_UP);
                    removeTextWatcherLitre();
                    litre.getEditText().setText(cons.toString());
                    recent = "litre";
                    addTextWatcherLitre();
                    if (!litrePrice.getEditText().getText().toString().equals("")) {
                        removeTextWatcherPrice();
                        Double fullPrice = Utils.calculateFullPrice(Double.parseDouble(litrePrice.getEditText().getText().toString()), Double.parseDouble(cons.toString()));
                        price.getEditText().setText(fullPrice + "");
                        addTextWatcherPrice();
                    } else if (!price.getEditText().getText().toString().equals("")) {
                        removeTextWatcherLitrePrice();
                        Double lPrice = Utils.calculateLitrePrice(Double.parseDouble(price.getEditText().getText().toString()), Double.parseDouble(cons.toString()));
                        litrePrice.getEditText().setText(lPrice + "");
                        addTextWatcherLitrePrice();
                    }
                } else if (!litre.getEditText().getText().toString().equals("") && !recent.equals("litre") && s.length() > 0) {
                    BigDecimal dist = new BigDecimal(litre.getEditText().getText().toString());
                    String cons = consumption.getEditText().getText().toString();
                    dist = dist.divide(new BigDecimal(cons), RoundingMode.HALF_UP);
                    dist = dist.multiply(BigDecimal.valueOf(100));
                    dist = dist.setScale(2, RoundingMode.HALF_UP);

                    removeTextWatcherKm();
                    km.getEditText().setText(dist.toString());
                    recent = "km";
                    addTextWatcherKm();
                }*/
            }
        };
    }

    private void removeTextWatcherConverted() {
        convertedValue.getEditText().removeTextChangedListener(forConverted);
    }

    private void addTextWatcherConverted() {
        convertedValue.getEditText().addTextChangedListener(forConverted);
    }

    private void removeTextWatcherOriginal() {
        originalValue.getEditText().removeTextChangedListener(forOriginal);
    }

    private void addTextWatcherOriginal() {
        originalValue.getEditText().addTextChangedListener(forOriginal);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spin = (Spinner)parent;
        Spinner spin2 = (Spinner)parent;

        if(spin.getId() == R.id.converter_from) {
            Log.d(TAG, "onItemSelected: from units");
        } else if(spin2.getId() == R.id.converter_to) {
            Log.d(TAG, "onItemSelected: to units");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

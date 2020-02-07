package com.example.fueldiet.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.R;
import com.example.fueldiet.Utils;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorFragment extends Fragment {

    TextInputLayout km;
    TextInputLayout litre;
    TextInputLayout price;
    TextInputLayout litrePrice;
    TextInputLayout consumption;

    String recent = "";

    TextWatcher forKm;
    TextWatcher forLitre;
    TextWatcher forPrice;
    TextWatcher forLitrePrice;
    TextWatcher forCons;

    public static CalculatorFragment newInstance() {
        CalculatorFragment fragment = new CalculatorFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        km = view.findViewById(R.id.calc_km_input);
        litre = view.findViewById(R.id.calc_litres_input);
        price = view.findViewById(R.id.calc_total_cost_input);
        litrePrice = view.findViewById(R.id.calc_price_per_l_input);
        consumption = view.findViewById(R.id.calc_cons_input);

        createTextWatchers();

        return view;
    }

    private void createTextWatchers() {
        forKm = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!litre.getEditText().getText().toString().equals("") && !recent.equals("litre") && s.length() > 0) {
                    Double cons = Utils.calculateConsumption(Integer.parseInt(km.getEditText().getText().toString()), Double.parseDouble(litre.getEditText().getText().toString()));
                    removeTextWatcherConsumption();
                    consumption.getEditText().setText(cons+"");
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
                }
            }
        };
        forCons = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!km.getEditText().getText().toString().equals("") && !recent.equals("km") && s.length() > 0) {
                    BigDecimal cons = new BigDecimal(consumption.getEditText().getText().toString());
                    cons = cons.divide(BigDecimal.valueOf(100));
                    cons = cons.multiply(new BigDecimal(km.getEditText().getText().toString()));
                    cons = cons.setScale(2, RoundingMode.HALF_UP);
                    removeTextWatcherLitre();
                    litre.getEditText().setText(cons.toString());
                    recent = "litre";
                    addTextWatcherLitre();
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
                }
            }
        };

        forLitre = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!km.getEditText().getText().toString().equals("") && !recent.equals("km") && s.length() > 0) {
                    Double cons = Utils.calculateConsumption(Integer.parseInt(km.getEditText().getText().toString()), Double.parseDouble(litre.getEditText().getText().toString()));
                    removeTextWatcherConsumption();
                    consumption.getEditText().setText(cons+"");
                    recent = "consumption";
                    addTextWatcherConsumption();
                } else if (!consumption.getEditText().getText().toString().equals("") && !recent.equals("consumption") && s.length() > 0) {
                    BigDecimal dist = new BigDecimal(litre.getEditText().getText().toString());
                    String tmp = litre.getEditText().getText().toString();
                    BigDecimal tmp1 = new BigDecimal(litre.getEditText().getText().toString());
                    dist = dist.divide(new BigDecimal(consumption.getEditText().getText().toString()), RoundingMode.HALF_UP);
                    dist = dist.multiply(BigDecimal.valueOf(100));
                    dist = dist.setScale(2, RoundingMode.HALF_UP);
                    removeTextWatcherKm();
                    km.getEditText().setText(dist.toString());
                    recent = "km";
                    addTextWatcherKm();
                }
            }
        };

        addTextWatcherKm();
        addTextWatcherConsumption();
        addTextWatcherLitre();
    }

    private void removeTextWatcherConsumption() {
        consumption.getEditText().removeTextChangedListener(forCons);
    }

    private void addTextWatcherConsumption() {
        consumption.getEditText().addTextChangedListener(forCons);
    }
    
    private void removeTextWatcherKm() {
        km.getEditText().removeTextChangedListener(forKm);
    }

    private void addTextWatcherKm() {
        km.getEditText().addTextChangedListener(forKm);
    }

    private void removeTextWatcherLitre() {
        litre.getEditText().removeTextChangedListener(forLitre);
    }

    private void addTextWatcherLitre() {
        litre.getEditText().addTextChangedListener(forLitre);
    }
}

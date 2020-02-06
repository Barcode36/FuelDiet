package com.example.fueldiet.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fueldiet.R;
import com.google.android.material.textfield.TextInputLayout;

public class CalculatorFragment extends Fragment {

    TextInputLayout km;
    TextInputLayout litre;
    TextInputLayout price;
    TextInputLayout litrePrice;
    TextInputLayout consumption;

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

        return view;
    }
}

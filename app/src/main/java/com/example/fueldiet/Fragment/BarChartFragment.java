package com.example.fueldiet.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.fueldiet.db.FuelDietDBHelper;

public class BarChartFragment extends Fragment {

    public BarChartFragment() { }

    private long vehicleID;
    private FuelDietDBHelper dbHelper;

    public static BarChartFragment newInstance(long id) {
        BarChartFragment fragment = new BarChartFragment();
        Bundle args = new Bundle();
        args.putLong("vehicleID", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleID = getArguments().getLong("vehicleID");
        }
        dbHelper = new FuelDietDBHelper(getContext());
    }
}

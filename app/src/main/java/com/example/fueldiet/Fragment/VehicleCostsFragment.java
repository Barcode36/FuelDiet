package com.example.fueldiet.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class VehicleCostsFragment extends Fragment {

    VehicleCostsFragment() {}

    public static VehicleCostsFragment newInstance(long id) {
        VehicleCostsFragment fragment = new VehicleCostsFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }
}

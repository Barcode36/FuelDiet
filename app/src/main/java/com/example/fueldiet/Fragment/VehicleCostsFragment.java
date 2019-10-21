package com.example.fueldiet.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.Activity.AddNewCostActivity;
import com.example.fueldiet.Adapter.CostAdapter;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class VehicleCostsFragment extends Fragment {

    private long id_vehicle;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    CostAdapter mAdapter;
    FuelDietDBHelper dbHelper;
    View view;

    public VehicleCostsFragment() {}

    public static VehicleCostsFragment newInstance(long id) {
        VehicleCostsFragment fragment = new VehicleCostsFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id_vehicle = getArguments().getLong("id");
        }
        dbHelper = new FuelDietDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vehicle_costs, container, false);
        mRecyclerView = view.findViewById(R.id.vehicle_costs_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager= new LinearLayoutManager(getActivity());
        mAdapter = new CostAdapter(getActivity(), dbHelper.getAllCosts(id_vehicle));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = view.findViewById(R.id.vehicle_costs_add_new);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddNewCostActivity.class);
            intent.putExtra("vehicle_id", id_vehicle);
            startActivity(intent);
        });

        return view;
    }
}

package com.example.fueldiet.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fueldiet.Activity.AddNewReminderActivity;
import com.example.fueldiet.Adapter.ReminderMultipleTypeAdapter;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class VehicleReminderFragment extends Fragment {

    private long id_vehicle;
    RecyclerView mRecyclerViewActive;
    LinearLayoutManager mLayoutManager;
    ReminderMultipleTypeAdapter mAdapter;
    FuelDietDBHelper dbHelper;
    View view;
    FloatingActionButton fab;

    public VehicleReminderFragment() {}

    public static VehicleReminderFragment newInstance(long id) {
        VehicleReminderFragment fragment = new VehicleReminderFragment();
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
        view = inflater.inflate(R.layout.fragment_vehicle_reminder, container, false);
        mRecyclerViewActive = view.findViewById(R.id.vehicle_reminder_recycler_view);
        mRecyclerViewActive.setHasFixedSize(true);
        mLayoutManager= new LinearLayoutManager(getActivity());
        List<ReminderObject> reminderList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllActiveReminders(id_vehicle);
        int pos = 0;
        while (cursor.moveToPosition(pos)) {
            reminderList.add(new ReminderObject(cursor.getInt(0), cursor.getLong(1), cursor.getInt(2), cursor.getString(5), cursor.getString(4), true));
            pos++;
        }
        cursor = dbHelper.getAllPreviousReminders(id_vehicle);
        pos = 0;
        while (cursor.moveToPosition(pos)) {
            reminderList.add(new ReminderObject(cursor.getInt(0), cursor.getLong(1), cursor.getInt(2), cursor.getString(5), cursor.getString(4), false));
            pos++;
        }
        cursor.close();

        mAdapter = new ReminderMultipleTypeAdapter(getActivity(), reminderList);
        mRecyclerViewActive.setAdapter(mAdapter);
        mRecyclerViewActive.setLayoutManager(mLayoutManager);

        mRecyclerViewActive.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        fab = view.findViewById(R.id.add_new_reminder);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AddNewReminderActivity.class);
            intent.putExtra("vehicle_id", id_vehicle);
            startActivity(intent);
        });

        return view;
    }
}

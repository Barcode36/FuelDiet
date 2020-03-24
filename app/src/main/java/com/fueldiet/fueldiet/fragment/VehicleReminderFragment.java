package com.fueldiet.fueldiet.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.activity.AddNewReminderActivity;
import com.fueldiet.fueldiet.activity.ConfirmReminderDoneActivity;
import com.fueldiet.fueldiet.adapter.ReminderMultipleTypeAdapter;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.R;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VehicleReminderFragment extends Fragment {

    private long id_vehicle;
    RecyclerView mRecyclerViewActive;
    LinearLayoutManager mLayoutManager;
    ReminderMultipleTypeAdapter mAdapter;
    FuelDietDBHelper dbHelper;
    View view;
    FloatingActionButton fab;
    List<ReminderObject> reminderList;

    private int tmpPos;
    private int tmpItm;

    public VehicleReminderFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        fillRemindersList();
        mAdapter.notifyDataSetChanged();
    }

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
        reminderList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vehicle_reminder, container, false);
        mRecyclerViewActive = view.findViewById(R.id.vehicle_reminder_recycler_view);
        mLayoutManager= new LinearLayoutManager(getActivity());
        fillRemindersList();
        mAdapter = new ReminderMultipleTypeAdapter(getActivity(), reminderList);
        mRecyclerViewActive.setAdapter(mAdapter);
        mRecyclerViewActive.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new ReminderMultipleTypeAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position, int element_id) {
                //TODO
            }

            @Override
            public void onDeleteClick(int position, int element_id) {
                tmpItm = element_id;
                tmpPos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.are_you_sure)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }

            @Override
            public void onDoneClick(int position, int element_id) {
                Intent intent = new Intent(getActivity(), ConfirmReminderDoneActivity.class);
                intent.putExtra("vehicle_id", id_vehicle);
                intent.putExtra("reminder_id", element_id);
                startActivity(intent);
            }
        });

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

    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                removeItem();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                Toast.makeText(getContext(), getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                break;
        }
    };

    private void removeItem() {
        ReminderObject deleted = dbHelper.getReminder(tmpItm);
        dbHelper.deleteReminder(tmpItm);
        fillRemindersList();
        mAdapter.notifyItemRemoved(tmpPos);

        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.object_deleted), Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) { }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) { }
        }).setAction("UNDO", v -> {
            dbHelper.addReminder(deleted);
            fillRemindersList();
            mAdapter.notifyItemInserted(tmpPos);
            mRecyclerViewActive.scrollToPosition(0);
            Toast.makeText(getContext(), getString(R.string.undo_pressed), Toast.LENGTH_SHORT).show();
        });
        snackbar.show();
    }

    private boolean fillRemindersList() {
        reminderList.clear();
        reminderList.add(new ReminderObject(-20));
        reminderList.addAll(dbHelper.getAllActiveReminders(id_vehicle));
        reminderList.add(new ReminderObject(-10));
        reminderList.addAll(dbHelper.getAllDoneReminders(id_vehicle));
        return true;
    }


    public static void quickDone(int element_id, Context context) {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(context);
        ReminderObject ro = dbHelper.getReminder(element_id);
        /*DriveObject driveObject = dbHelper.getPrevDrive(ro.getCarID());
        CostObject costObject = dbHelper.getPrevCost(ro.getCarID());
        ReminderObject reminderObject = dbHelper.getBiggestReminder(ro.getCarID());*/
        VehicleObject vehicleObject = dbHelper.getVehicle(ro.getCarID());

        /*
        int biggestODO = Math.max(vehicleObject.getOdoKm(), Math.max(
                costObject == null ? -1 : costObject.getKm(), Math.max(
                        driveObject == null ? -1 : driveObject.getOdo(),
                        reminderObject == null ? -1 : reminderObject.getKm()
                )
        ));*/
        int biggestODO = vehicleObject.getOdoFuelKm();
        Date tm = Calendar.getInstance().getTime();

        if (ro.getKm() == null)
            ro.setKm(biggestODO);
        else
            ro.setDate(tm);
        dbHelper.updateReminder(ro);
    }
}

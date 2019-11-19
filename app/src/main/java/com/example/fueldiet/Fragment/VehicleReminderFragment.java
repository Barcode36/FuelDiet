package com.example.fueldiet.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import com.example.fueldiet.Activity.AddNewReminderActivity;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Activity.VehicleDetailsActivity;
import com.example.fueldiet.Adapter.ReminderMultipleTypeAdapter;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.R;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
        //mRecyclerViewActive.setHasFixedSize(true);
        mLayoutManager= new LinearLayoutManager(getActivity());
        fillRemindersList();
        mAdapter = new ReminderMultipleTypeAdapter(getActivity(), reminderList);
        mRecyclerViewActive.setAdapter(mAdapter);
        mRecyclerViewActive.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new ReminderMultipleTypeAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position, int element_id) {

                //mAdapter.notifyItemChanged(position);
                //mAdapter.notifyItemMoved(position, getNewPosition(old));
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
                done(element_id, getContext());
                ReminderObject old = reminderList.get(position);
                //Toast.makeText(getContext(), "Position: "+position, Toast.LENGTH_SHORT).show();
                fillRemindersList();
                mAdapter.notifyItemMoved(position, getNewPosition(old));
                //updateFragment();
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
        delete(tmpItm);
        Toast.makeText(getContext(), getString(R.string.object_deleted), Toast.LENGTH_SHORT).show();
        fillRemindersList();
        mAdapter.notifyItemRemoved(tmpPos);
    }

    private boolean fillRemindersList() {
        reminderList.clear();
        reminderList.add(new ReminderObject(-20));
        /*
        Cursor cursor = dbHelper.getAllActiveReminders(id_vehicle);
        int pos = 0;
        while (cursor.moveToPosition(pos)) {
            reminderList.add(new ReminderObject(
                    cursor.getInt(0),
                    cursor.getLong(1),
                    cursor.getInt(2),
                    cursor.getString(5),
                    cursor.getString(4),
                    true,
                    cursor.getLong(3))
            );
            pos++;
        }

         */

        reminderList.addAll(dbHelper.getAllActiveReminders(id_vehicle));
        reminderList.add(new ReminderObject(-10));
        /*
        cursor = dbHelper.getAllPreviousReminders(id_vehicle);
        pos = 0;
        while (cursor.moveToPosition(pos)) {
            reminderList.add(new ReminderObject(
                    cursor.getInt(0),
                    cursor.getLong(1),
                    cursor.getInt(2),
                    cursor.getString(5),
                    cursor.getString(4),
                    false,
                    cursor.getLong(3))
            );
            pos++;
        }
        cursor.close();

         */
        reminderList.addAll(dbHelper.getAllPreviousReminders(id_vehicle));
        return true;
    }

    public static void done(int element_id, Context context) {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(context);
        ReminderObject ro = dbHelper.getReminder(element_id);
        DriveObject driveObject = dbHelper.getPrevDrive(ro.getCarID());
        int km = 0;
        Date tm = Calendar.getInstance().getTime();
        if (driveObject != null) {
            km = driveObject.getOdo();
            tm = driveObject.getDate().getTime();
        }
        if (ro.getKm() == null)
            ro.setKm(km);
        else
            ro.setDate(tm);
        dbHelper.updateReminder(ro);
    }

    private void delete(int element_id) {
        dbHelper.deleteReminder(element_id);
    }

    private int getNewPosition(ReminderObject old) {
        Integer newPosition = null;
        for (ReminderObject ro : reminderList) {
            if (ro.getId() > 0 && ro.getTitle().equals(old.getTitle())) {
                if (old.getKm() == null && ro.getDate().equals(old.getDate())) {
                    newPosition = reminderList.indexOf(ro);
                } else if (old.getKm().equals(ro.getKm())) {
                    newPosition = reminderList.indexOf(ro);
                }
            }
        }
        return newPosition;
    }

    private void updateFragment() {
        Intent intent = new Intent(getActivity(), VehicleDetailsActivity.class);
        intent.putExtra("vehicle_id", id_vehicle);
        intent.putExtra("frag", 2);
        startActivity(intent);
    }
}

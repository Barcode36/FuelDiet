package com.fueldiet.fueldiet.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fueldiet.fueldiet.Utils;
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
import java.util.Comparator;
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
            public void onRepeatDoneClick(int position, int element_id) {
                nextNotification(position, element_id);
            }

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

    private void fillRemindersList() {
        reminderList.clear();
        reminderList.add(new ReminderObject(-20));
        reminderList.addAll(dbHelper.getAllActiveTimeReminders(id_vehicle));
        reminderList.addAll(dbHelper.getAllActiveOdoReminders(id_vehicle));
        reminderList.add(new ReminderObject(-15));
        reminderList.addAll(dbHelper.getAllActiveRepeatReminders(id_vehicle));
        reminderList.add(new ReminderObject(-10));
        reminderList.addAll(dbHelper.getAllDoneReminders(id_vehicle));
    }

    private void nextNotification(int position, int element_id) {
        ReminderObject reminder = dbHelper.getReminder(element_id);

        if (reminder.getDate() == null) {
            //km mode
            VehicleObject vehicleObject = dbHelper.getVehicle(reminder.getCarID());
            int biggestODO = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
            biggestODO = Math.max(biggestODO, vehicleObject.getOdoRemindKm());

            int repeatNumber = Integer.parseInt(reminder.getDesc().split("//-")[0]);
            if (biggestODO >= reminder.getKm() + reminder.getRepeat() * repeatNumber) {
                resetRepeatNotification(element_id, getContext());
                fillRemindersList();
                mAdapter.notifyItemChanged(position);
            }
        } else {
            //date mode
            Calendar calendar = Calendar.getInstance();
            Date remDate = reminder.getDate();
            int repeatNumber = Integer.parseInt(reminder.getDesc().split("//-")[0]);
            Calendar next = Calendar.getInstance();
            next.setTimeInMillis(remDate.getTime());
            next.add(Calendar.DATE, reminder.getRepeat() * repeatNumber);

            if (calendar.after(next)) {
                resetRepeatNotification(element_id, getContext());
                fillRemindersList();
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    public static void resetRepeatNotification(int id, Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;

        /*if (c == null) {
            //we have km reminder, use date from notification
            c = Calendar.getInstance();
            StatusBarNotification[] all = manager.getActiveNotifications();
            for (StatusBarNotification sbn : all) {
                if (sbn.getId() == id) {
                    c.setTimeInMillis(sbn.getPostTime());
                    break;
                }
            }
        }*/
        manager.cancel(id);
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(context);
        ReminderObject reminder = dbHelper.getReminder(id);
        String[] desc = reminder.getDesc().split("//-");
        int rpt = Integer.parseInt(desc[0]);
        rpt++;
        if (desc.length < 2)
            reminder.setDesc(rpt+"//-");
        else
            reminder.setDesc(rpt + "//-" + desc[1]);
        dbHelper.updateReminder(reminder);
        //c.add(Calendar.MINUTE, 1);
        //Utils.startAlarm(c, id, context, idVehicle);
    }
}

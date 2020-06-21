package com.fueldiet.fueldiet.fragment;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
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

import com.fueldiet.fueldiet.AlertReceiver;
import com.fueldiet.fueldiet.AutomaticBackup;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.activity.AddNewReminderActivity;
import com.fueldiet.fueldiet.activity.ConfirmReminderDoneActivity;
import com.fueldiet.fueldiet.activity.EditReminderActivity;
import com.fueldiet.fueldiet.adapter.ReminderMultipleTypeAdapter;
import com.fueldiet.fueldiet.object.CostObject;
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

import static androidx.constraintlayout.widget.Constraints.TAG;

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
                if (!reminderList.get(position).isActive()) {
                    Toast.makeText(getContext(), "Work in progress! Sorry!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), EditReminderActivity.class);
                intent.putExtra("vehicle_id", id_vehicle);
                intent.putExtra("reminder_id", element_id);
                startActivity(intent);
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
        ReminderObject latest = dbHelper.getLatestDoneReminder(id_vehicle);
        dbHelper.deleteReminder(tmpItm);
        fillRemindersList();
        mAdapter.notifyItemRemoved(tmpPos);

        /*
        if (deleted.getDate() != null && deleted.getKm() == null) {
            //remove alarm
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(deleted.getId());
        }*/

        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.object_deleted), Snackbar.LENGTH_LONG);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) { }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                try {
                    //delete reminder
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(getContext(), AlertReceiver.class);
                    intent.putExtra("vehicle_id", deleted.getCarID());
                    intent.putExtra("reminder_id", deleted.getId());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), deleted.getId(), intent, 0);
                    alarmManager.cancel(pendingIntent);
                    Log.d(TAG, "onDismissed: deleted alarm");

                    //change max km in vehicle if needed
                    if (deleted.getId() == latest.getId()) {
                        VehicleObject vehicleObject = dbHelper.getVehicle(id_vehicle);
                        ReminderObject newLatest = dbHelper.getLatestDoneReminder(id_vehicle);
                        List<CostObject> resetCosts = dbHelper.getAllCostWithReset(id_vehicle);
                        if (resetCosts.size() != 0) {
                            if (resetCosts.get(0).getDate().before(newLatest.getDate()))
                                vehicleObject.setOdoRemindKm(newLatest.getKm());
                            else
                                vehicleObject.setOdoRemindKm(0);
                        } else {
                            vehicleObject.setOdoRemindKm(newLatest.getKm());
                        }
                        dbHelper.updateVehicle(vehicleObject);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onDismissed: " + e.getMessage());
                }
            }
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

        AutomaticBackup automaticBackup = new AutomaticBackup(context);
        automaticBackup.createBackup(context);
    }
}

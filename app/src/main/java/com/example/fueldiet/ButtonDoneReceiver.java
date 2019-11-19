package com.example.fueldiet;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.fueldiet.Fragment.VehicleReminderFragment;

public class ButtonDoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        Log.e("MSG", "Button click registered");

        VehicleReminderFragment
                .newInstance(vehicle_id)
                .quickDone(reminderID, context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(reminderID);
    }
}

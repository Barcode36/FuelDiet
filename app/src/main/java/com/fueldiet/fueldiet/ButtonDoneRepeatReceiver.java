package com.fueldiet.fueldiet;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.fragment.VehicleReminderFragment;
import com.fueldiet.fueldiet.object.ReminderObject;

public class ButtonDoneRepeatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        Log.e("MSG", "Done repeat button click registered");

        VehicleReminderFragment.resetRepeatNotification(reminderID, context);
    }
}

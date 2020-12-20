package com.fueldiet.fueldiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.fueldiet.fueldiet.fragment.VehicleReminderFragment;

public class ButtonDoneRepeatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", 1);
        Log.e("MSG", "Done repeat button click registered");

        VehicleReminderFragment.resetRepeatNotification(reminderID, context);
    }
}

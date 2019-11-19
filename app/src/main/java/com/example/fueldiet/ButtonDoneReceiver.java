package com.example.fueldiet;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.fueldiet.Activity.BaseActivity;
import com.example.fueldiet.Fragment.VehicleReminderFragment;

public class ButtonDoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        Log.e("MSG", "Button click registered");

        VehicleReminderFragment
                .newInstance(vehicle_id)
                .done(reminderID, context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(reminderID);
    }
}

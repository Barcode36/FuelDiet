package com.fueldiet.fueldiet;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.fueldiet.fueldiet.fragment.VehicleReminderFragment;

import java.util.Calendar;

/**
 * Receiver for buttons on notification
 */
public class ButtonDismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", (long) 1);
        Log.e("MSG", "Dismiss button click registered");

        /*VehicleReminderFragment
                .newInstance(vehicle_id)
                .quickDone(reminderID, context);*/

        long when = 0;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] all = manager.getActiveNotifications();
        for (StatusBarNotification notification : all) {
            when = notification.getPostTime();
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(when);
        manager.cancel(reminderID);
        Utils.startAlarm(c, reminderID, context, vehicle_id);
    }
}

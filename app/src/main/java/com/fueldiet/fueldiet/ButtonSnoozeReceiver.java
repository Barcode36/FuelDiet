package com.fueldiet.fueldiet;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Receiver for buttons on notification
 */
public class ButtonSnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderID = intent.getIntExtra("reminder_id", -2);
        long vehicle_id = intent.getLongExtra("vehicle_id", 1);
        Log.e("MSG", "Snooze button/swipe click registered");

        long notifyTime = new Date().getTime();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] all = manager.getActiveNotifications();

        for (StatusBarNotification sbn : all) {
            if (sbn.getId() == reminderID) {
                notifyTime = sbn.getPostTime();
                break;
            }
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(notifyTime);
        c.add(Calendar.DAY_OF_MONTH, 1);
        manager.cancel(reminderID);
        Utils.startAlarm(c, reminderID, context, vehicle_id);
    }
}

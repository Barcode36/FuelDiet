package com.example.fueldiet;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Activity.VehicleDetailsActivity;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.db.FuelDietDBHelper;

public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "remindersID";
    public static final String channelName = "Reminders";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification(int reminderID) {
        FuelDietDBHelper dbHelper = new FuelDietDBHelper(this);
        ReminderObject ro = dbHelper.getReminder(reminderID);
        VehicleObject vo = dbHelper.getVehicle(ro.getCarID());
        Intent activityIntent = new Intent(getApplicationContext(), VehicleDetailsActivity.class);
        //Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        long carid = ro.getCarID();
        activityIntent.putExtra("vehicle_id", carid);
        activityIntent.putExtra("frag", 2);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle(vo.getMake() + " " + vo.getModel() + " " +
                        ro.getTitle())
                .setContentText(ro.getDesc())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);
    }
}
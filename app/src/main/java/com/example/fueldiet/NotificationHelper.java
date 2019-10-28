package com.example.fueldiet;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Activity.VehicleDetailsActivity;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.db.FuelDietDBHelper;

import java.io.File;

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

        ManufacturerObject mo = MainActivity.manufacturers.get(vo.getMake());
        Bitmap bitmap;
        try {
            File storageDIR = getApplicationContext().getDir("Images", MODE_PRIVATE);
            bitmap = BitmapFactory.decodeFile(storageDIR + "/" + mo.getFileName());
        } catch (Exception e) {
            bitmap = Utils.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_help_outline_black_24dp);
        }

        long carid = ro.getCarID();
        activityIntent.putExtra("vehicle_id", carid);
        activityIntent.putExtra("frag", 2);
        activityIntent.putExtra("reminder_id", reminderID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle(vo.getMake() + " " + vo.getModel() + " " + ro.getTitle())
                .setContentText(ro.getDesc())
                .setColor(getColor(R.color.colorPrimary))
                .addAction(R.mipmap.ic_launcher, "OPEN APP", pendingIntent);
    }
}
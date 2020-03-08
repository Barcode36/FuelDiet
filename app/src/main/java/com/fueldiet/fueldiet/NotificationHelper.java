package com.fueldiet.fueldiet;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.fueldiet.fueldiet.activity.MainActivity;
import com.fueldiet.fueldiet.activity.VehicleDetailsActivity;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;

import java.io.File;

import static com.fueldiet.fueldiet.Utils.toCapitalCaseWords;


/**
 * Custom Notification Builder
 */
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

        Bitmap bitmap;
        try {
            String fileName = vo.getCustomImg();
            File storageDIR = getApplicationContext().getDir("Images",MODE_PRIVATE);
            if (fileName == null) {
                ManufacturerObject mo = MainActivity.manufacturers.get(toCapitalCaseWords(vo.getMake()));
                int idResource = getApplicationContext().getResources().getIdentifier(mo.getFileNameModNoType(), "drawable", getApplicationContext().getPackageName());
                bitmap = BitmapFactory.decodeFile(storageDIR+"/"+mo.getFileNameMod());
            } else {
                bitmap = BitmapFactory.decodeFile(storageDIR + "/" + fileName);
            }
        } catch (Exception e) {
            bitmap = Utils.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_help_outline_black_24dp);
        }

        long carid = ro.getCarID();
        Intent activityIntentOpen = new Intent(getApplicationContext(), VehicleDetailsActivity.class);
        activityIntentOpen.putExtra("vehicle_id", carid);
        activityIntentOpen.putExtra("frag", 2);
        activityIntentOpen.putExtra("reminder_id", reminderID);
        PendingIntent pendingIntentOpen = PendingIntent.getActivity(this, 0, activityIntentOpen, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDone = new Intent(getApplicationContext(), ButtonDoneReceiver.class);
        intentDone.putExtra("vehicle_id", carid);
        intentDone.putExtra("reminder_id", reminderID);
        PendingIntent pendingIntentDone = PendingIntent.getBroadcast(this, 0, intentDone, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        New custom
         */

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        remoteViews.setTextViewText(R.id.notification_text_title, vo.getMake() + " " + vo.getModel());
        remoteViews.setTextViewText(R.id.notification_text_subtitle, ro.getTitle());
        remoteViews.setTextViewText(R.id.notification_text_description, ro.getDesc());
        remoteViews.setImageViewBitmap(R.id.notification_image_logo_car, bitmap);

        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.drawable.ic_notification_icon_logo)
                .setColor(getColor(R.color.colorPrimary))
                .addAction(R.mipmap.ic_launcher, getString(R.string.open), pendingIntentOpen)
                .addAction(R.mipmap.ic_launcher, getString(R.string.q_done), pendingIntentDone)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
    }
}
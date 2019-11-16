package com.example.fueldiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.fueldiet.Activity.MainActivity;
import com.example.fueldiet.Object.CostObject;
import com.example.fueldiet.Object.DriveObject;
import com.example.fueldiet.Object.ManufacturerObject;
import com.example.fueldiet.Object.ReminderObject;
import com.example.fueldiet.db.FuelDietContract;
import com.example.fueldiet.db.FuelDietDBHelper;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Utils {


    public static String toCapitalCaseWords(String string) {
        if (string.length() == 0)
            return string;
        String[] arr = string.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String s : arr) {
            sb.append(Character.toUpperCase(s.charAt(0)))
                    .append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static double calculateConsumption(int trip, double l) {
        BigDecimal bd = new BigDecimal(Double.toString(l/trip*100));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double convertUnitToKmPL(double kmp100) {
        BigDecimal bd = new BigDecimal(Double.toString(100/kmp100));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double calculateFullPrice(double p, double l) {
        BigDecimal bd = new BigDecimal(Double.toString(p*l));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double calculateLitrePrice(double p, double l) {
        BigDecimal bd = new BigDecimal(Double.toString(p/l));
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static List<ReminderObject> getReminderObjectFromCursor(Cursor c, boolean status) {
        List<ReminderObject> reminderObjectList = new ArrayList<>();
        int pos = 0;
        while (c.moveToPosition(pos)) {
            reminderObjectList.add(new ReminderObject(
                    c.getInt(0),
                    c.getLong(1),
                    c.getInt(2),
                    c.getString(5),
                    c.getString(4),
                    status,
                    c.getLong(3))
            );
            pos++;
        }
        c.close();
        return reminderObjectList;
    }

    public static void startAlarm(Calendar c, int reminderID, Context context, long vehicleID) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.putExtra("vehicle_id", vehicleID);
        intent.putExtra("reminder_id", reminderID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderID, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.MINUTE, 1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public static void checkKmAndSetAlarms(long vehicleID, FuelDietDBHelper dbHelper, Context context) {
        DriveObject driveObject = dbHelper.getPrevDrive(vehicleID);

        int lastODO;
        if (driveObject == null)
            lastODO = -1;
        else
            lastODO = driveObject.getOdo();

        List<ReminderObject> activeVehicleReminders = dbHelper.getAllActiveReminders(vehicleID);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        for (ReminderObject ro : activeVehicleReminders) {
            if (ro.getKm() != null && ro.getKm() <= lastODO) {
                startAlarm(calendar, ro.getId(), context, vehicleID);
                calendar.add(Calendar.SECOND, 2);
            }
        }
    }

    public static String fromSLOtoENG(String type) {
        switch (type) {
            case "Registracija":
                return "Registration";
            case "Cestnine":
                return "Tolls";
            case "Servis":
                return "Service";
            case "Modificiranje":
                return "Modification";
            case "Vzdrževanje":
                return "Maintenance";
            case "Drugo":
                return "Other";
            case "Bencin":
                return "Petrol";
            case "Dizel":
                return "Diesel";
            case "Hibrid":
                return "Hybrid";
            case "Električni":
                return "Electric";
            default:
                return type;
        }
    }

    public static String fromENGtoSLO(String type) {
        switch (type) {
            case "Registration":
                return "Registracija";
            case "Tolls":
                return "Cestnine";
            case "Service":
                return "Servis";
            case "Modification":
                return "Modificiranje";
            case "Maintenance":
                return "Vzdrževanje";
            case "Other":
                return "Drugo";
            case "Petrol":
                return "Bencin";
            case "Diesel":
                return "Dizel";
            case "Hybrid":
                return "Hibrid";
            case "Electric":
                return "Električni";
            default:
                return type;
        }
    }

    public static List<Integer> getColoursSet() {
        // add a lot of colours
        ArrayList<Integer> colours = new ArrayList<>();
        for (int col : ColorTemplate.VORDIPLOM_COLORS)
            colours.add(col);
        for (int col : ColorTemplate.JOYFUL_COLORS)
            colours.add(col);
        for (int col : ColorTemplate.COLORFUL_COLORS)
            colours.add(col);
        for (int col : ColorTemplate.LIBERTY_COLORS)
            colours.add(col);
        for (int col : ColorTemplate.PASTEL_COLORS)
            colours.add(col);
        colours.add(ColorTemplate.getHoloBlue());
        return colours;
    }

    public static List<CostObject> createCostObject(Cursor c) {
        List<CostObject> costObjects = new ArrayList<>();

        int pos = 0;
        while (c.moveToPosition(pos)) {
            costObjects.add(new CostObject(
                    c.getLong(4),
                    c.getLong(1),
                    c.getDouble(3),
                    c.getInt(2),
                    c.getString(5),
                    c.getString(6),
                    c.getString(7),
                    c.getLong(0))
            );
            pos++;
        }
        c.close();
        return costObjects;
    }

    public static List<DriveObject> createDriveObject(Cursor c) {
        List<DriveObject> driveObjects = new ArrayList<>();

        int pos = 0;
        while (c.moveToPosition(pos)) {
            driveObjects.add(new DriveObject(
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_ODO_KM)),
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP_KM)),
                    c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry._ID))
            ));
            pos++;
        }
        c.close();
        return driveObjects;
    }


    public static void downloadImage(Resources resources, Context context, ManufacturerObject mo) {
        int px = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 65, resources.getDisplayMetrics()));
        //File storageDIR = context.getDir("Images", MODE_PRIVATE);
        Glide.with(context)
                .asBitmap()
                .load(mo.getUrl())
                .fitCenter()
                .override(px)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File storageDIR = context.getDir("Images",MODE_PRIVATE);
                        boolean success = true;
                        if (!storageDIR.exists()) {
                            success = storageDIR.mkdirs();
                        }
                        if (success) {
                            File imageFile = new File(storageDIR, mo.getFileNameMod());
                            try {
                                OutputStream fOut = new FileOutputStream(imageFile);
                                if (mo.getFileNameMod().contains("png"))
                                    resource.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                else
                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                fOut.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
        ManufacturerObject real = MainActivity.manufacturers.get(mo.getName());
        real.setOriginal(true);
    }

    private static void saveImage(Context context, String fileName, Bitmap image) {
        File storageDIR = context.getDir("Images",MODE_PRIVATE);
        boolean success = true;
        if (!storageDIR.exists()) {
            success = storageDIR.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDIR, fileName);
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                if (fileName.contains("png"))
                    image.compress(Bitmap.CompressFormat.PNG, 90, fOut);
                else
                    image.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

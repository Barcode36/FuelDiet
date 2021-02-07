package com.fueldiet.fueldiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fueldiet.fueldiet.activity.MainActivity;
import com.fueldiet.fueldiet.db.FuelDietContract;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;
import com.fueldiet.fueldiet.object.ManufacturerObject;
import com.fueldiet.fueldiet.object.PetrolStationObject;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Utils {
    private static final String TAG = "Utils";

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
    public static double calculateConsumptionKmPL(int trip, double l) {
        BigDecimal bd = new BigDecimal(Double.toString(trip/l));
        bd = bd.setScale(1, RoundingMode.HALF_UP);
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
                    c.getInt(c.getColumnIndex(FuelDietContract.ReminderEntry._ID)),
                    c.getLong(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DATE)),
                    c.getInt(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO)),
                    c.getString(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_TITLE)),
                    c.getString(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DETAILS)),
                    status,
                    c.getLong(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_CAR)),
                    c.getInt(c.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_REPEAT)))
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
            c.add(Calendar.SECOND, 1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public static void checkKmAndSetAlarms(long vehicleID, FuelDietDBHelper dbHelper, Context context) {

        VehicleObject vehicleObject = dbHelper.getVehicle(vehicleID);
        int biggestODO = Math.max(vehicleObject.getOdoFuelKm(), vehicleObject.getOdoCostKm());
        biggestODO = Math.max(biggestODO, vehicleObject.getOdoRemindKm());

        List<ReminderObject> activeVehicleReminders = dbHelper.getAllActiveReminders(vehicleID);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        for (ReminderObject ro : activeVehicleReminders) {
            if (ro.getRepeat() != 0) {
                int dist = ro.getRepeat();
                int repeatNumber = Integer.parseInt(ro.getDesc().split("//-")[0]);
                int newDist = ro.getKm() + (dist * repeatNumber);
                if (newDist <= biggestODO) {
                    startAlarm(calendar, ro.getId(), context, vehicleID);
                    calendar.add(Calendar.MILLISECOND, 500);
                }
            }
            else if (ro.getKm() != null && ro.getKm() <= biggestODO) {
                startAlarm(calendar, ro.getId(), context, vehicleID);
                calendar.add(Calendar.MILLISECOND, 500);
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
            case "Kazni":
                return "Tickets/Fines";
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
            case "Tickets/Fines":
                return "Kazni";
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

    public static void downloadPSImage(Context context, PetrolStationObject ps) {
        Glide.with(context)
                .asBitmap()
                .load(ps.getLogo(context))
                .fitCenter()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File storageDIR = context.getDir("Images",MODE_PRIVATE);
                        boolean success = true;
                        if (!storageDIR.exists()) {
                            success = storageDIR.mkdirs();
                        }
                        if (success) {
                            File imageFile = new File(storageDIR, ps.getFileName());
                            try {
                                OutputStream fOut = new FileOutputStream(imageFile);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    public static void downloadPSImage(Context context, Uri uri, String fileName) {
        Glide.with(context)
                .asBitmap()
                .load(uri)
                .fitCenter()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File storageDIR = context.getDir("Images",MODE_PRIVATE);
                        boolean success = true;
                        if (!storageDIR.exists()) {
                            success = storageDIR.mkdirs();
                        }
                        if (success) {
                            File imageFile = new File(storageDIR, fileName);
                            try {
                                OutputStream fOut = new FileOutputStream(imageFile);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }


    public static void downloadImage(Resources resources, Context context, Uri uri, String title) {
        int px = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 65, resources.getDisplayMetrics()));
        Glide.with(context)
                .asBitmap()
                .load(uri)
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
                            File imageFile = new File(storageDIR, title);
                            try {
                                OutputStream fOut = new FileOutputStream(imageFile);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                fOut.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    public static boolean readCsvFile(@NonNull InputStream inputStream, Context context) {
        FuelDietDBHelper dbHelper = FuelDietDBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.resetDb();
        // String output = context.getString(R.string.import_done);
        Log.d(TAG, "readCsvFile: Starting to restore data from backup");
        try {
            //InputStream inputStream = context.getContentResolver().openInputStream(uri);
            //FileReader file = new FileReader(filePath);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
            ContentValues cv = new ContentValues();
            String line = "";
            db.beginTransaction();

            Log.d(TAG, "readCSVfile: file has been imported to buffer reader");

            String current = "";
            while ((line = buffer.readLine()) != null) {
                Log.d(TAG, "readCSVfile: reading new line from file");
                if (line.substring(1,line.length()-1).equals("Vehicles:") ||
                        line.substring(1,line.length()-1).equals("Drives:") ||
                        line.substring(1,line.length()-1).equals("Costs:") ||
                        line.substring(1,line.length()-1).equals("Reminders:") ||
                        line.substring(1,line.length()-1).equals("Petrol Station:")) {
                    Log.d(TAG, "readCSVfile: line is header");
                    current = line.substring(1,line.length()-1);
                } else {
                    String [] splitLine;
                    switch (current) {
                        case "Vehicles:":
                            Log.d(TAG, "readCSVfile: line is vehicle");
                            splitLine = line.split(",");
                            if (splitLine[0].substring(1, splitLine[0].length() - 1).equals("_id"))
                                break;

                            long id = Long.parseLong(splitLine[0].substring(1, splitLine[0].length() - 1));
                            String make = splitLine[1].substring(1, splitLine[1].length() - 1);
                            String model = splitLine[2].substring(1, splitLine[2].length() - 1);
                            double engine = Double.parseDouble(splitLine[3].substring(1, splitLine[3].length() - 1));
                            String fuelType = splitLine[4].substring(1, splitLine[4].length() - 1);
                            String hybridType = splitLine[5].substring(1, splitLine[5].length() - 1);
                            int modelYear = Integer.parseInt(splitLine[6].substring(1, splitLine[6].length() - 1));
                            int hp = Integer.parseInt(splitLine[7].substring(1, splitLine[7].length() - 1));
                            int torque = Integer.parseInt(splitLine[8].substring(1, splitLine[8].length() - 1));
                            int odoFuel = Integer.parseInt(splitLine[9].substring(1, splitLine[9].length() - 1));
                            int odoCost = Integer.parseInt(splitLine[10].substring(1, splitLine[10].length() - 1));
                            int odoRemind = Integer.parseInt(splitLine[11].substring(1, splitLine[11].length() - 1));
                            // 12 is img
                            String trans = splitLine[13].substring(1, splitLine[13].length() - 1);

                            cv.clear();
                            cv.put(FuelDietContract.VehicleEntry._ID, id);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, engine);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, fuelType);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, hp);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_TORQUE, torque);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, make);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, model);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_FUEL_KM, odoFuel);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_COST_KM, odoCost);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_REMIND_KM, odoRemind);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_HYBRID_TYPE, hybridType);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL_YEAR, modelYear);
                            cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, trans);

                            Log.d(TAG, "readCSVfile: inserting vehicle in to the db");
                            db.insert(FuelDietContract.VehicleEntry.TABLE_NAME, null, cv);
                            break;
                        case "Drives:":
                            Log.d(TAG, "readCSVfile: line is drive");
                            splitLine = line.split(",");
                            if (splitLine[0].substring(1, splitLine[0].length() - 1).equals("_id"))
                                break;

                            long id1 = Long.parseLong(splitLine[0].substring(1, splitLine[0].length() - 1));
                            long date1 = Long.parseLong(splitLine[1].substring(1, splitLine[1].length() - 1));
                            int odo1 = Integer.parseInt(splitLine[2].substring(1, splitLine[2].length() - 1));
                            int trip1 = Integer.parseInt(splitLine[3].substring(1, splitLine[3].length() - 1));
                            double price1 = Double.parseDouble(splitLine[4].substring(1, splitLine[4].length() - 1));
                            double litre1 = Double.parseDouble(splitLine[5].substring(1, splitLine[5].length() - 1));
                            long car1 = Long.parseLong(splitLine[6].substring(1, splitLine[6].length() - 1));
                            int first1 = Integer.parseInt(splitLine[7].substring(1, splitLine[7].length() - 1));
                            int full1 = Integer.parseInt(splitLine[8].substring(1, splitLine[8].length() - 1));
                            
                            String note1 = null;
                            if (!splitLine[9].equals("")) {
                                note1 = splitLine[9].substring(1, splitLine[9].length() - 1);
                                note1 = note1.replaceAll(";;", ",");
                            }
                            String country1 = splitLine[10].substring(1, splitLine[10].length() - 1);
                            Double lat1 = null;
                            if (!splitLine[11].equals("")) {
                                lat1 = Double.parseDouble(splitLine[11].substring(1, splitLine[11].length() - 1));
                            }
                            Double long1 = null;
                            if (!splitLine[12].equals("")) {
                                long1 = Double.parseDouble(splitLine[12].substring(1, splitLine[12].length() - 1));
                            }
                            String petrolStation1 = splitLine[13].substring(1, splitLine[13].length() - 1);


                            cv.clear();
                            cv.put(FuelDietContract.DriveEntry._ID, id1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_CAR, car1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_DATE, date1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_ODO, odo1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_TRIP, trip1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_LITRES, litre1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE, price1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_NOTE, note1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_PETROL_STATION, petrolStation1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_COUNTRY, country1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_FIRST, first1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_NOT_FULL, full1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_LATITUDE, lat1);
                            cv.put(FuelDietContract.DriveEntry.COLUMN_LONGITUDE, long1);

                            Log.d(TAG, "readCSVfile: inserting drive in to the db");
                            db.insert(FuelDietContract.DriveEntry.TABLE_NAME, null, cv);
                            break;
                        case "Costs:":
                            Log.d(TAG, "readCSVfile: line is cost");
                            splitLine = line.split(",");
                            if (splitLine[0].substring(1, splitLine[0].length() - 1).equals("_id"))
                                break;

                            long id2 = Long.parseLong(splitLine[0].substring(1, splitLine[0].length() - 1));
                            long date2 = Long.parseLong(splitLine[1].substring(1, splitLine[1].length() - 1));
                            int odo2 = Integer.parseInt(splitLine[2].substring(1, splitLine[2].length() - 1));
                            double price2 = Double.parseDouble(splitLine[3].substring(1, splitLine[3].length() - 1));
                            long car2 = Long.parseLong(splitLine[4].substring(1, splitLine[4].length() - 1));
                            String note2 = null;
                            if (!splitLine[5].equals("")) {
                                note2 = splitLine[5].substring(1, splitLine[5].length() - 1);
                                note2 = note2.replaceAll(";;", ",");
                            }

                            String title2 = splitLine[6].substring(1,splitLine[6].length()-1);
                            String type2 = splitLine[7].substring(1,splitLine[7].length()-1);
                            int reset2 = Integer.parseInt(splitLine[8].substring(1,splitLine[8].length()-1));

                            cv.clear();
                            cv.put(FuelDietContract.CostsEntry._ID, id2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_CAR, car2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_PRICE, price2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_TITLE, title2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_DETAILS, note2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_ODO, odo2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_DATE, date2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_TYPE, type2);
                            cv.put(FuelDietContract.CostsEntry.COLUMN_RESET_KM, reset2);

                            Log.d(TAG, "readCSVfile: inserting cost in to the db");
                            db.insert(FuelDietContract.CostsEntry.TABLE_NAME, null, cv);
                            break;
                        case "Reminders:":
                            Log.d(TAG, "readCSVfile: line is reminder");
                            splitLine = line.split(",");
                            if (splitLine[0].substring(1, splitLine[0].length()-1).equals("_id"))
                                break;

                            long id3 = Long.parseLong(splitLine[0].substring(1,splitLine[0].length()-1));
                            Long date3;
                            if (splitLine[1].equals(""))
                                date3 = null;
                            else
                                date3 = Long.parseLong(splitLine[1].substring(1,splitLine[1].length()-1));
                            Integer odo3;
                            if (splitLine[2].equals(""))
                                odo3 = null;
                            else
                                odo3 = Integer.parseInt(splitLine[2].substring(1,splitLine[2].length()-1));
                            int repeat = Integer.parseInt(splitLine[3].substring(1, splitLine[3].length()-1));
                            long car3 = Long.parseLong(splitLine[4].substring(1,splitLine[4].length()-1));
                            String note3;
                            if (splitLine[5].equals(""))
                                note3 = "";
                            else
                                note3 = splitLine[5].substring(1,splitLine[5].length()-1);

                            String title3 = splitLine[6].substring(1,splitLine[6].length()-1);

                            cv.clear();
                            cv.put(FuelDietContract.ReminderEntry._ID, id3);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_CAR, car3);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_DATE, date3);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_DETAILS, note3);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_ODO, odo3);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_REPEAT, repeat);
                            cv.put(FuelDietContract.ReminderEntry.COLUMN_TITLE, title3);

                            Log.d(TAG, "readCSVfile: inserting reminder in to the db");
                            long insertedId = db.insert(FuelDietContract.ReminderEntry.TABLE_NAME, null, cv);

                            if (insertedId != -1) {
                                //if it's -1 then there was an error
                                //check if it's km or date reminder, if date than check if repeat
                                ReminderObject reminderObject = dbHelper.getReminder((int)insertedId);

                                if (reminderObject.getKm() == null && reminderObject.getDate() != null) {
                                    Log.d(TAG, "readCSVfile: reminder is date type");
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(reminderObject.getDate());
                                    if (reminderObject.getRepeat() != 0) {
                                        Log.d(TAG, "readCSVfile: reminder is repeating type, modifying calendar");
                                        //it's repeated
                                        String [] desc = reminderObject.getDesc().split("//-");
                                        String repeated = desc[0];
                                        int repeatInterval = reminderObject.getRepeat();
                                        calendar.add(Calendar.DAY_OF_MONTH, repeatInterval + (repeatInterval * Integer.parseInt(repeated)));
                                    }
                                    Log.d(TAG, "readCSVfile: creating remind alert");
                                    Utils.startAlarm(calendar, reminderObject.getId(), context, reminderObject.getCarID());
                                }
                            }
                            
                            break;
                        case "Petrol Station:":
                            Log.d(TAG, "readCSVfile: line is petrol station");
                            splitLine = line.split(",");
                            if (splitLine[0].substring(1, splitLine[0].length()-1).equals("name"))
                                break;

                            String name4 = splitLine[0].substring(1, splitLine[0].length()-1);
                            int origin = Integer.parseInt(splitLine[1].substring(1,splitLine[1].length()-1));
                            byte[] logo4 = Base64.decode(splitLine[2].substring(1,splitLine[2].length()-1), Base64.NO_WRAP);

                            cv.clear();
                            cv.put(FuelDietContract.PetrolStationEntry.COLUMN_NAME, name4);
                            cv.put(FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN, origin);
                            cv.put(FuelDietContract.PetrolStationEntry.COLUMN_LOGO, logo4);

                            Log.d(TAG, "readCSVfile: inserting petrol station in to the db");
                            db.insert(FuelDietContract.PetrolStationEntry.TABLE_NAME, null, cv);
                            break;
                    }
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            Log.d(TAG, "readCsvFile: restoring was successful");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "readCsvFile: " + e.getMessage(), e.fillInStackTrace());
            if (db.inTransaction())
                db.endTransaction();
            return false;
        }
    }

    public static boolean createCsvFile(@NonNull OutputStream outputStream, Context context) {

        FuelDietDBHelper dbHelper = FuelDietDBHelper.getInstance(context);
        try {
            //OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
            CSVWriter csvWrite = new CSVWriter(bw);
            SQLiteDatabase sdb = dbHelper.getReadableDatabase();
            Cursor curCSV = sdb.rawQuery("SELECT * FROM " + FuelDietContract.VehicleEntry.TABLE_NAME, null);
            csvWrite.writeNext(new String[]{"Vehicles:"});
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String[] arrStr = {
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry._ID)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MODEL)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HYBRID_TYPE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MODEL_YEAR)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_TORQUE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_FUEL_KM)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_COST_KM)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_REMIND_KM)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.writeNext(new String[]{"Drives:"});
            curCSV = sdb.rawQuery("SELECT * FROM " + FuelDietContract.DriveEntry.TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String note = curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOTE));
                if (note != null && !note.equals("")) {
                    note = note.replace(",", ";;");
                }
                String lat = curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LATITUDE));
                if (lat != null && !lat.equals("")) {
                    lat = lat.replace(",", ";;");
                }
                String longi = curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LONGITUDE));
                if (longi != null && !longi.equals("")) {
                    longi = longi.replace(",", ";;");
                }
                
                String[] arrStr = {
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry._ID)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_ODO)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CAR)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_FIRST)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOT_FULL)),
                        note,
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_COUNTRY)),
                        lat,
                        longi,
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PETROL_STATION))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.writeNext(new String[]{"Costs:"});
            curCSV = sdb.rawQuery("SELECT * FROM " + FuelDietContract.CostsEntry.TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String details = curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DETAILS));
                if (details == null || details.equals("")) {
                } else {
                    details = details.replace(",", ";;");
                }
                String[] arrStr = {
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry._ID)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_DATE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_ODO)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_PRICE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_CAR)),
                        details,
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TITLE)).replace("\\,", "\\;"),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_TYPE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.CostsEntry.COLUMN_RESET_KM))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.writeNext(new String[]{"Reminders:"});
            curCSV = sdb.rawQuery("SELECT * FROM " + FuelDietContract.ReminderEntry.TABLE_NAME, null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String details = curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DETAILS));
                if (details == null || details.equals("")) {
                } else {
                    details = details.replace(",", ";;");
                }
                String[] arrStr = {
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry._ID)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_DATE)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_ODO)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_REPEAT)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_CAR)),
                        details,
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.ReminderEntry.COLUMN_TITLE)).replace("\\,", "\\;")
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.writeNext(new String[]{"Petrol Station:"});
            curCSV = sdb.rawQuery("SELECT " + FuelDietContract.PetrolStationEntry.COLUMN_NAME + "," + FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN + "," + FuelDietContract.PetrolStationEntry.COLUMN_LOGO + " FROM " + FuelDietContract.PetrolStationEntry.TABLE_NAME + " WHERE "+ FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN + " = 1", null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Which column you want to export
                String[] arrStr = {
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.PetrolStationEntry.COLUMN_NAME)),
                        curCSV.getString(curCSV.getColumnIndex(FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN)),
                        Base64.encodeToString(curCSV.getBlob(curCSV.getColumnIndex(FuelDietContract.PetrolStationEntry.COLUMN_LOGO)), Base64.NO_WRAP)
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            return true;
        } catch (Exception sqlEx) {
            Log.e(TAG, "createCSVfile: "+sqlEx.getMessage(), sqlEx.fillInStackTrace());
            return false;
        }
    }




    public static List<PetrolStationObject> getPetrolStationFromCursor(Cursor c) {
        List<PetrolStationObject> petrolStationObjects = new ArrayList<>();
        int pos = 0;
        while (c.moveToPosition(pos)) {
            petrolStationObjects.add(new PetrolStationObject(
                    c.getInt(c.getColumnIndex(FuelDietContract.PetrolStationEntry._ID)),
                    c.getString(c.getColumnIndex(FuelDietContract.PetrolStationEntry.COLUMN_NAME)),
                    c.getInt(c.getColumnIndex(FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN))
            ));
            pos++;
        }
        c.close();
        return petrolStationObjects;
    }
}

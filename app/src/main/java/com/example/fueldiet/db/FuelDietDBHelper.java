package com.example.fueldiet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fueldiet.object.CostObject;
import com.example.fueldiet.object.DriveObject;
import com.example.fueldiet.object.ReminderObject;
import com.example.fueldiet.object.VehicleObject;
import com.example.fueldiet.Utils;
import com.example.fueldiet.db.FuelDietContract.*;

import java.util.ArrayList;
import java.util.List;


public class FuelDietDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fueldiet.db";
    public static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;


    public FuelDietDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        this.db = db;

        final String SQL_CREATE_VEHICLES_TABLE = "CREATE TABLE " +
                VehicleEntry.TABLE_NAME + "(" +
                VehicleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VehicleEntry.COLUMN_MAKE + " TEXT NOT NULL, " +
                VehicleEntry.COLUMN_MODEL + " TEXT NOT NULL, " +
                VehicleEntry.COLUMN_ENGINE + " TEXT NOT NULL, " +
                VehicleEntry.COLUMN_FUEL_TYPE + " TEXT NOT NULL, " +
                VehicleEntry.COLUMN_HP + " INT NOT NULL, " +
                VehicleEntry.COLUMN_ODO_KM + " INT NOT NULL DEFAULT 0, " +
                VehicleEntry.COLUMN_CUSTOM_IMG + " TEXT DEFAULT NULL, " +
                VehicleEntry.COLUMN_TRANSMISSION + " TEXT NOT NULL);";

        final String SQL_CREATE_DRIVES_TABLE = "CREATE TABLE " +
                DriveEntry.TABLE_NAME + "(" +
                DriveEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DriveEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_ODO_KM + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_TRIP_KM + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_PRICE_LITRE + " REAL NOT NULL, " +
                DriveEntry.COLUMN_LITRES + " REAL NOT NULL, " +
                DriveEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_FIRST + " INTEGER NOT NULL DEFAULT 0, " +
                DriveEntry.COLUMN_NOT_FULL + " INTEGER NOT NULL DEFAULT 0, " +
                DriveEntry.COLUMN_NOTE + " TEXT DEFAULT NULL, " +
                DriveEntry.COLUMN_PETROL_STATION + " TEXT NOT NULL DEFAULT 'Other', " +
                "FOREIGN KEY (" + DriveEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

        final String SQL_CREATE_COSTS_TABLE = "CREATE TABLE " +
                CostsEntry.TABLE_NAME + "(" +
                CostsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CostsEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                CostsEntry.COLUMN_ODO + " INTEGER NOT NULL, " +
                CostsEntry.COLUMN_EXPENSE + " REAL NOT NULL, " +
                CostsEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                CostsEntry.COLUMN_DETAILS + " TEXT, " +
                CostsEntry.COLUMN_TITLE + " TEXT NOT NULL, "  +
                CostsEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                CostsEntry.COLUMN_RESET_KM + " INTEGER NOT NULL DEFAULT 0, "+
                "FOREIGN KEY (" + CostsEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

        final String SQL_CREATE_REMINDERS_TABLE = "CREATE TABLE " +
                ReminderEntry.TABLE_NAME + "(" +
                ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReminderEntry.COLUMN_DATE + " INTEGER, " +
                ReminderEntry.COLUMN_ODO + " INTEGER, " +
                ReminderEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                ReminderEntry.COLUMN_DETAILS + " TEXT, " +
                ReminderEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + ReminderEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_DRIVES_TABLE);
        db.execSQL(SQL_CREATE_COSTS_TABLE);
        db.execSQL(SQL_CREATE_REMINDERS_TABLE);

        createVehicles();
        createDrives();
        createCosts();
        createReminders();

    }

    private void createVehicles() {
        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(1, 'Maserati', 'Levante GTS', '3.8L V8', 'Petrol', 'Automatic', 550)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + "," + VehicleEntry.COLUMN_ODO_KM + ") VALUES " +
                "(2, 'Alfa Romeo', 'Giulia QV', '2.9L V6', 'Petrol', 'Automatic', 512, 7273)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(3, 'Renault', 'Megane RS Trophy', '1.8L TCe', 'Petrol', 'Manual', 320)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(4, 'Alpine', 'A110', '1.8L TCe', 'Petrol', 'Automatic', 250)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(6, 'Land Rover', 'Range Rover Velar SVO', '5.0L V8', 'Petrol', 'Automatic', 575)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ", " + VehicleEntry.COLUMN_ODO_KM + ") VALUES " +
                "(5, 'Mini', 'Cooper 1300', '1.3L I4', 'Petrol', 'Manual', 45, 45237)");

    }

    private void createDrives() {
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " +
                        DriveEntry.COLUMN_PETROL_STATION + ", " + DriveEntry.COLUMN_FIRST + ") VALUES " +
                        "(1, 1562941295, 2, 0, 1.324, 59.21, 2, 'Petrol', 1)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(2, 1563177015, 652, 650, 1.324, 55.0, 2, 'Eni')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(3, 1563516941, 1252, 600, 1.294, 54.0, 2, 'Mol')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(4, 1563727966, 1946, 694, 1.251, 50.0, 2, 'Mol')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(5, 1563878467, 2408, 462, 1.540, 58.0, 2, 'Petrol')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(6, 1564979147, 2930, 522, 1.540, 54.0, 2, 'Eni')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(7, 1565966867, 3551, 621, 1.292, 57.32, 2, 'OMV')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(8, 1566576204, 3931, 380, 1.311, 35.0, 2, 'Avanti')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(9, 1567191766, 4554, 623, 1.309, 50.0, 2, 'OMV')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(10, 1567611381, 5130, 576, 1.681, 52.31, 2, 'OMV')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(11, 1567675281, 5583, 453, 1.861, 54.87, 2, 'Petrol')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(12, 1568360915, 6104, 521, 1.267, 49.47, 2, 'Petrol')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(13, 1569585598, 6652, 548, 1.272, 57.14, 2, 'Ina')");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ", " + DriveEntry.COLUMN_PETROL_STATION + ") VALUES " +
                        "(14, 1570694569, 7273, 621, 1.222, 55.23, 2, 'Avanti')");
    }

    private void createCosts() {
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " +
                CostsEntry.COLUMN_DETAILS + ", " + CostsEntry.COLUMN_CAR +
                ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(1, 1562933251, 2, 87250, 'Bought new car', 'Cost of the car, with discound (12%) and " +
                "an extra promotional gear and a coupon for a new set of winter performance tyres.', 2," +
                "'Other')");
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " +
                CostsEntry.COLUMN_DETAILS + ", " + CostsEntry.COLUMN_CAR + ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(2, 1562934251, 2, 250, 'Registration', 'Cost of car registration', 2, 'Registration')");
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " +
                CostsEntry.COLUMN_CAR + ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(3, 1563537371, 1255, 342, 'First service', 2, 'Service')");
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " +
                CostsEntry.COLUMN_CAR + ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(4, 1562943358, 36, 110, 'Vignette', 2, 'Tolls')");
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " +
                CostsEntry.COLUMN_DETAILS + ", " + CostsEntry.COLUMN_CAR + ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(5, 1563693718, 1503, 105, 'Vignette', 'Austria', 2, 'Tolls')");
        db.execSQL("INSERT INTO " + CostsEntry.TABLE_NAME + " (" + CostsEntry._ID + ", " +
                CostsEntry.COLUMN_DATE + ", " + CostsEntry.COLUMN_ODO + ", " +
                CostsEntry.COLUMN_EXPENSE + ", " + CostsEntry.COLUMN_TITLE + ", " + CostsEntry.COLUMN_DETAILS + ", " +
                CostsEntry.COLUMN_CAR + ", " + CostsEntry.COLUMN_TYPE + ") VALUES " +
                "(6, 1565004407, 3271, 1799.99, 'PPF', 'Paint protection foil', 2, 'Maintenance')");
    }

    private void createReminders() {
        db.execSQL("INSERT INTO " + ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + ", " +
                ReminderEntry.COLUMN_DATE + ", " + ReminderEntry.COLUMN_ODO + ", " +
                ReminderEntry.COLUMN_TITLE + ", " + ReminderEntry.COLUMN_CAR + ") VALUES " +
                "(1, 1563537371, 1000, 'First service', 2)");
        db.execSQL("INSERT INTO " + ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + ", " +
                ReminderEntry.COLUMN_DATE + ", " + ReminderEntry.COLUMN_ODO + ", " +
                ReminderEntry.COLUMN_TITLE + ", " + ReminderEntry.COLUMN_CAR + ") VALUES " +
                "(2, 1563964786, 2250, 'Change cabin filtre', 2)");
        db.execSQL("INSERT INTO " + ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + ", " +
                ReminderEntry.COLUMN_DATE + ", " + ReminderEntry.COLUMN_ODO + ", " +
                ReminderEntry.COLUMN_TITLE + ", " + ReminderEntry.COLUMN_DETAILS + ", " + ReminderEntry.COLUMN_CAR + ") VALUES " +
                "(3, 1567191769, 4000, 'Tyres', 'Check tyres for wear and tear, check tyres pressure', 2)");
        db.execSQL("INSERT INTO " + ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + ", " +
                ReminderEntry.COLUMN_ODO + ", " + ReminderEntry.COLUMN_TITLE + ", " +
                ReminderEntry.COLUMN_DETAILS + ", " + ReminderEntry.COLUMN_CAR + ") VALUES " +
                "(4, 7000, 'Modification', 'ECU tuning', 2)");
        db.execSQL("INSERT INTO " + ReminderEntry.TABLE_NAME + " (" + ReminderEntry._ID + ", " +
                ReminderEntry.COLUMN_DATE + ", " + ReminderEntry.COLUMN_TITLE + ", " +
                ReminderEntry.COLUMN_DETAILS + ", " + ReminderEntry.COLUMN_CAR + ") VALUES " +
                "(5, 1594472735, 'Registration', 'Registration will expire', 2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CostsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        onCreate(db);*/

        //write sql for each new change
        //create switch and check old version, do not use break
    }

    public boolean resetDb() {
        db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CostsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        onCreate(db);
        return true;
    }

    public VehicleObject getVehicle(long id) {
        db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + VehicleEntry.TABLE_NAME + " WHERE " + VehicleEntry._ID + " = " + id, null);

        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createVehicleObjects(c).get(0);
    }

    public void addVehicle(VehicleObject vo) {
        db = getWritableDatabase();

        ContentValues cv = vo.getContentValues();
        db.insert(VehicleEntry.TABLE_NAME, null, cv);
    }

    public void updateVehicle(VehicleObject vo) {
        db = getWritableDatabase();

        ContentValues cv = vo.getContentValues();
        db.update(VehicleEntry.TABLE_NAME, cv, VehicleEntry._ID + " = " + vo.getId(), null);
    }

    public List<VehicleObject> getAllVehicles() {
        db = getReadableDatabase();
        Cursor c = db.query(
                VehicleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                VehicleEntry.COLUMN_MAKE + " ASC"
        );
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createVehicleObjects(c);
    }
    public List<VehicleObject> getAllVehiclesExcept(long id) {
        db = getReadableDatabase();
        Cursor c = db.query(
                VehicleEntry.TABLE_NAME,
                null,
                VehicleEntry._ID + " != " + id,
                null,
                null,
                null,
                VehicleEntry.COLUMN_MAKE + " ASC"
        );
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createVehicleObjects(c);
    }

    public void deleteVehicle(long id) {
        db = getWritableDatabase();
        db.delete(DriveEntry.TABLE_NAME,
                DriveEntry.COLUMN_CAR + " = " + id, null);
        db.delete(CostsEntry.TABLE_NAME,
                DriveEntry.COLUMN_CAR + " = " + id, null);
        db.delete(ReminderEntry.TABLE_NAME,
                DriveEntry.COLUMN_CAR + " = " + id, null);
        db.delete(FuelDietContract.VehicleEntry.TABLE_NAME,
                FuelDietContract.VehicleEntry._ID + "=" + id, null);
    }

    public void updateDriveODO(DriveObject driveObject) {
        db = getWritableDatabase();

        ContentValues cv = driveObject.getContentValues();
        db.update(DriveEntry.TABLE_NAME, cv, DriveEntry._ID + " = " + driveObject.getId(), null);
    }

    public List<DriveObject> getAllDrives(long vehicleID) {
        List<DriveObject> drives = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.query(
                DriveEntry.TABLE_NAME,
                null,
                DriveEntry.COLUMN_CAR + " = " +vehicleID,
                null,
                null,
                null,
                DriveEntry.COLUMN_DATE + " DESC"
        );
        while (c.moveToNext()) {
            drives.add(new DriveObject(
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(DriveEntry._ID)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL))
            ));
        }
        c.close();
        return drives;
    }

    public DriveObject getPrevDrive(long id) {
        db = getReadableDatabase();
        //iskati največji datum?
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + id + " AND " + DriveEntry.COLUMN_ODO_KM + " = " +
                " ( SELECT MAX(" + DriveEntry.COLUMN_DATE + ") FROM " + DriveEntry.TABLE_NAME +
                " WHERE " + DriveEntry.COLUMN_CAR + " = " + id + ")", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)));
        c.close();
        return dv;
    }

    public DriveObject getPrevDriveSelection(long vehicleID, long nextDate) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + vehicleID + " AND " + DriveEntry.COLUMN_DATE + " < " +
                nextDate + " ORDER BY " + DriveEntry.COLUMN_DATE + " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)));
        c.close();
        return dv;
    }

    public DriveObject getNextDriveSelection(long vehicleID, long nextDate) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + vehicleID + " AND " + DriveEntry.COLUMN_DATE + " > " +
                nextDate + " ORDER BY " + DriveEntry.COLUMN_DATE + " ASC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)));
        c.close();
        return dv;
    }

    public DriveObject getFirstDrive(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + vehicleID + " ORDER BY " + DriveEntry.COLUMN_DATE +
                " ASC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)));
        c.close();
        return dv;
    }

    public DriveObject getLastDrive(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + vehicleID + " ORDER BY " + DriveEntry.COLUMN_DATE +
                " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)));
        c.close();
        return dv;
    }

    public void addDrive(DriveObject driveObject) {
        ContentValues cv = driveObject.getContentValues();
        db = getWritableDatabase();
        db.insert(DriveEntry.TABLE_NAME, null, cv);
    }

    public List<DriveObject> getAllDrivesWhereTimeBetween(long vehicleID, long smallerTime, long biggerTime) {
        db = getReadableDatabase();
        List<DriveObject> drives = new ArrayList<>();
        Cursor c = db.query(
                DriveEntry.TABLE_NAME,
                null,
                DriveEntry.COLUMN_CAR + " = " +vehicleID + " AND " + DriveEntry.COLUMN_DATE
                        + " >= " + smallerTime + " AND " + DriveEntry.COLUMN_DATE + " <= " + biggerTime,
                null,
                null,
                null,
                DriveEntry.COLUMN_DATE + " ASC"
        );
        while (c.moveToNext()) {
            drives.add(new DriveObject(
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO_KM)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP_KM)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(DriveEntry._ID)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL))
            ));
        }
        c.close();
        return drives;
    }

    public DriveObject getDrive(long driveID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " + DriveEntry._ID + " = " + driveID, null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createDriveObject(c).get(0);
    }

    public void removeLastDrive(long vehicleID) {
        db = getWritableDatabase();
        db.delete(DriveEntry.TABLE_NAME,
                DriveEntry.COLUMN_CAR + " = " + vehicleID + " AND " + DriveEntry.COLUMN_ODO_KM + " = (SELECT MAX(" + DriveEntry.COLUMN_ODO_KM + ") FROM " + DriveEntry.TABLE_NAME + " WHERE " + DriveEntry.COLUMN_CAR + " = " + vehicleID +")", null);

    }

    public List<CostObject> getAllCosts(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                CostsEntry.TABLE_NAME,
                null,
                CostsEntry.COLUMN_CAR + " = " +vehicleID,
                null,
                null,
                null,
                CostsEntry.COLUMN_DATE + " DESC"
        );
        return Utils.createCostObject(c);
    }

    public List<CostObject> getAllActualCostsFromType(long vehicleID, String type) {
        db = getReadableDatabase();
        Cursor c = db.query(
                CostsEntry.TABLE_NAME,
                null,
                CostsEntry.COLUMN_CAR + " = " +vehicleID + " AND " + CostsEntry.COLUMN_TYPE + " = '" + type + "'",
                null,
                null,
                null,
                CostsEntry.COLUMN_DATE + " DESC"
        );
        return Utils.createCostObject(c);
    }

    public CostObject getPrevCost(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " ORDER BY " + CostsEntry.COLUMN_ODO +
                " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }

    public ReminderObject getBiggestReminder(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + ReminderEntry.TABLE_NAME + " WHERE " +
                ReminderEntry.COLUMN_CAR + " = " + vehicleID + " AND " + ReminderEntry.COLUMN_DATE +
                " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL ORDER BY " +
                ReminderEntry.COLUMN_ODO + " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.getReminderObjectFromCursor(c, true).get(0);
    }

    public CostObject getPrevCost(long vehicleID, int km) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " AND " +
                CostsEntry.COLUMN_ODO + " < " + km + " ORDER BY " + CostsEntry.COLUMN_ODO + " DESC " +
                " LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }


    public CostObject getNextCost(long vehicleID, int km) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " AND " +
                CostsEntry.COLUMN_ODO + " > " + km + " ORDER BY " + CostsEntry.COLUMN_ODO + " ASC " +
                " LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }

    public void addCost(CostObject costObject) {
        db = getWritableDatabase();
        db.insert(CostsEntry.TABLE_NAME, null, costObject.getContentValues());
    }

    public void updateCost(CostObject costObject) {
        db = getWritableDatabase();
        db.update(CostsEntry.TABLE_NAME, costObject.getContentValues(), CostsEntry._ID + " = " + costObject.getCostID(), null);
    }

    public CostObject getFirstCost(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " ORDER BY " + CostsEntry.COLUMN_DATE +
                " ASC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }
    public CostObject getLastCost(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " ORDER BY " + CostsEntry.COLUMN_DATE +
                " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }

    public List<CostObject> getAllCostsWhereTimeBetween(long vehicleID, long smallerTime, long biggerTime) {
        db = getReadableDatabase();
        Cursor c = db.query(
                CostsEntry.TABLE_NAME,
                null,
                CostsEntry.COLUMN_CAR + " = " +vehicleID + " AND " + CostsEntry.COLUMN_DATE
                        + " >= " + smallerTime + " AND " + CostsEntry.COLUMN_DATE + " <= " + biggerTime,
                null,
                null,
                null,
                CostsEntry.COLUMN_DATE + " DESC"
        );
        return Utils.createCostObject(c);
    }

    public CostObject getCost(long costID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                CostsEntry.TABLE_NAME,
                null,
                CostsEntry._ID + " = " + costID,
                null,
                null,
                null,
                null
        );
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.createCostObject(c).get(0);
    }

    public void removeCost(long costID) {
        db = getWritableDatabase();
        db.delete(CostsEntry.TABLE_NAME,
                CostsEntry._ID + " = " + costID , null);
    }

    public List<ReminderObject> getAllActiveReminders(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry.COLUMN_CAR + " = " +vehicleID + " AND (" + ReminderEntry.COLUMN_DATE + " IS NULL OR " + ReminderEntry.COLUMN_ODO + " IS NULL)",
                null,
                null,
                null,
                ReminderEntry.COLUMN_ODO + " DESC, " + ReminderEntry.COLUMN_DATE + " ASC"
        );
        return Utils.getReminderObjectFromCursor(c, true);
    }

    public List<ReminderObject> getAllPreviousReminders(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry.COLUMN_CAR + " = " +vehicleID + " AND " + ReminderEntry.COLUMN_DATE + " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL",
                null,
                null,
                null,
                ReminderEntry.COLUMN_DATE + " DESC"
        );
        return Utils.getReminderObjectFromCursor(c, false);
    }

    public int addReminder(long vehicle_id, String title, long date, String desc) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderEntry.COLUMN_CAR, vehicle_id);
        cv.put(ReminderEntry.COLUMN_TITLE, title);
        cv.put(ReminderEntry.COLUMN_DETAILS, desc);
        cv.put(ReminderEntry.COLUMN_DATE, date);

        db = getWritableDatabase();
        db.insert(ReminderEntry.TABLE_NAME, null, cv);
        Cursor c = db.rawQuery("SELECT MAX(" + ReminderEntry._ID + ") FROM " + ReminderEntry.TABLE_NAME + " WHERE " + ReminderEntry.COLUMN_CAR + " = " + vehicle_id, null);
        c.moveToFirst();
        return c.getInt(0);
    }
    public int addReminder(long vehicle_id, String title, int odo, String desc) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderEntry.COLUMN_CAR, vehicle_id);
        cv.put(ReminderEntry.COLUMN_TITLE, title);
        cv.put(ReminderEntry.COLUMN_DETAILS, desc);
        cv.put(ReminderEntry.COLUMN_ODO, odo);

        db = getWritableDatabase();
        db.insert(ReminderEntry.TABLE_NAME, null, cv);
        Cursor c = db.rawQuery("SELECT MAX(" + ReminderEntry._ID + ") FROM " + ReminderEntry.TABLE_NAME + " WHERE " + ReminderEntry.COLUMN_CAR + " = " + vehicle_id, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public void addReminder(ReminderObject ro) {
        ContentValues cv = ro.getContentValues();

        db = getWritableDatabase();
        db.insert(ReminderEntry.TABLE_NAME, null, cv);
    }

    public ReminderObject getReminder(int reminderID) {
        db = getReadableDatabase();
        Cursor cursor = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry._ID + " = " + reminderID,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        boolean status = false;
        if (cursor.getLong(1) != 0 && cursor.getInt(2) != 0)
            status = true;
        return Utils.getReminderObjectFromCursor(cursor, status).get(0);
    }

    public ReminderObject getPrevReminder(ReminderObject ro) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + ReminderEntry.TABLE_NAME + " WHERE " +
                ReminderEntry.COLUMN_CAR + " = " + ro.getCarID() + " AND " + ReminderEntry.COLUMN_ODO +
                " < " + ro.getKm() + " AND " + ReminderEntry.COLUMN_DATE +
                " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL ORDER BY " +
                ReminderEntry.COLUMN_ODO + " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.getReminderObjectFromCursor(c, true).get(0);
    }

    public ReminderObject getNextReminder(ReminderObject ro) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + ReminderEntry.TABLE_NAME + " WHERE " +
                ReminderEntry.COLUMN_CAR + " = " + ro.getCarID() + " AND " + ReminderEntry.COLUMN_DATE +
                " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL AND " +
                ReminderEntry.COLUMN_ODO + " > " + ro.getKm() + " ORDER BY " +
                ReminderEntry.COLUMN_ODO + " ASC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.getReminderObjectFromCursor(c, true).get(0);
    }

    public void updateReminder(ReminderObject ro) {
        db = getWritableDatabase();
        ContentValues cv = ro.getContentValues();
        db.update(ReminderEntry.TABLE_NAME, cv, ReminderEntry._ID + " = " + ro.getId(), null);
    }

    public void deleteReminder(int remID) {
        db = getWritableDatabase();
        db.delete(ReminderEntry.TABLE_NAME,
                ReminderEntry._ID + " = " + remID, null);
    }
}

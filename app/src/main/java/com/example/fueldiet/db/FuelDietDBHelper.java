package com.example.fueldiet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fueldiet.Object.VehicleObject;
import com.example.fueldiet.db.FuelDietContract.*;


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
                CostsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                CostsEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + CostsEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_DRIVES_TABLE);
        db.execSQL(SQL_CREATE_COSTS_TABLE);

        createVehicles();
        createDrives();
        createCosts();

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
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(2, 'Alfa Romeo', 'Giulia QV', '2.9L V6', 'Petrol', 'Automatic', 512)");

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
                "(5, 'Land Rover', 'Range Rover Velar SVO', '5.0L V8', 'Petrol', 'Automatic', 575)");

        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + " (" + VehicleEntry._ID + ", " +
                VehicleEntry.COLUMN_MAKE + ", " + VehicleEntry.COLUMN_MODEL + ", " +
                VehicleEntry.COLUMN_ENGINE + ", " + VehicleEntry.COLUMN_FUEL_TYPE + ", " +
                VehicleEntry.COLUMN_TRANSMISSION + ", " + VehicleEntry.COLUMN_HP + ") VALUES " +
                "(6, 'Mini', 'Cooper 1300', '1.3L I4', 'Petrol', 'Manual', 45)");

    }

    private void createDrives() {
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(1, 1563177015, 652, 650, 1.324, 55.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(2, 1563516941, 1252, 600, 1.294, 54.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(3, 1563727966, 1946, 694, 1.251, 50.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(4, 1563878467, 2408, 462, 1.540, 58.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(5, 1564979147, 2930, 522, 1.540, 54.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(6, 1565966867, 3551, 621, 1.292, 57.32, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_ODO_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_PRICE_LITRE + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(7, 1566576204, 3931, 380, 1.311, 35.0, 2)");
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CostsEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean resetDb() {
        db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CostsEntry.TABLE_NAME);
        onCreate(db);
        return true;
    }

    public VehicleObject getVehicle(long id) {
        db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + VehicleEntry.TABLE_NAME + " WHERE " + VehicleEntry._ID + " = " + id, null);

        VehicleObject vo = new VehicleObject();
        if (c.moveToFirst()) {
            vo.setMake(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_MAKE)));
            vo.setModel(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_MODEL)));
            vo.setEngine(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_ENGINE)));
            vo.setFuel(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_FUEL_TYPE)));
            vo.setHp(c.getInt(c.getColumnIndex(VehicleEntry.COLUMN_HP)));
            vo.setTransmission(c.getString(c.getColumnIndex(VehicleEntry.COLUMN_TRANSMISSION)));
        }

        c.close();
        return vo;
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

    public Cursor getAllVehicles() {
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
        return c;
    }
    public Cursor getAllVehiclesExcept(long id) {
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
        return c;
    }

    public void deleteVehicle(long id) {
        db = getReadableDatabase();
        db.delete(DriveEntry.TABLE_NAME,
                DriveEntry.COLUMN_CAR + " = " + id, null);
        db.delete(FuelDietContract.VehicleEntry.TABLE_NAME,
                FuelDietContract.VehicleEntry._ID + "=" + id, null);
    }

    public Cursor getAllDrives(long vehicleID) {
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
        return c;
    }

    public Cursor getPrevDrive(long id) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + DriveEntry.COLUMN_ODO_KM + "), " +
                DriveEntry.COLUMN_TRIP_KM + " FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + id, null);
        c.moveToFirst();
        return c;
    }

    public Cursor getPrevDriveSelection(long id, int nextKM) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + DriveEntry.COLUMN_ODO_KM + "), " +
                DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_TRIP_KM + ", " +
                DriveEntry.COLUMN_PRICE_LITRE + " FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + id + " AND " + DriveEntry.COLUMN_ODO_KM + " < " +
                nextKM, null);
        c.moveToFirst();
        return c;
    }

    public String getFirstDrive(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(" + DriveEntry.COLUMN_DATE + ") FROM " + DriveEntry.TABLE_NAME + " WHERE " + DriveEntry.COLUMN_CAR + " = " + vehicleID, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c.getString(0);
    }
    public String getLastDrive(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + DriveEntry.COLUMN_DATE + ") FROM " + DriveEntry.TABLE_NAME + " WHERE " + DriveEntry.COLUMN_CAR + " = " + vehicleID, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c.getString(0);
    }

    public void addDrive(long vehicle_id, double fuelLitres, double fuelPrice, int odo, int trip, long date) {
        ContentValues cv = new ContentValues();
        cv.put(DriveEntry.COLUMN_CAR, vehicle_id);
        cv.put(DriveEntry.COLUMN_LITRES, fuelLitres);
        cv.put(DriveEntry.COLUMN_PRICE_LITRE, fuelPrice);
        cv.put(DriveEntry.COLUMN_TRIP_KM, trip);
        cv.put(DriveEntry.COLUMN_ODO_KM, odo);
        cv.put(DriveEntry.COLUMN_DATE, date);

        db = getWritableDatabase();
        db.insert(DriveEntry.TABLE_NAME, null, cv);
    }

    public Cursor getAllCosts(long vehicleID) {
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
        return c;
    }

    public Cursor getPrevCost(long vehicleID, int km) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + CostsEntry.COLUMN_ODO + "), " + CostsEntry.COLUMN_DATE + " FROM " + CostsEntry.TABLE_NAME + " WHERE " + CostsEntry.COLUMN_CAR + " = " + vehicleID + " AND " + CostsEntry.COLUMN_ODO + " < " + km, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c;
    }
    public Cursor getNextCost(long vehicleID, int km) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(" + CostsEntry.COLUMN_ODO + "), " + CostsEntry.COLUMN_DATE + " FROM " + CostsEntry.TABLE_NAME + " WHERE " + CostsEntry.COLUMN_CAR + " = " + vehicleID + " AND " + CostsEntry.COLUMN_ODO + " > " + km, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c;
    }


    public void addCost(long vehicle_id, double price, String title, int odo, String desc, String type, long date) {
        ContentValues cv = new ContentValues();
        cv.put(CostsEntry.COLUMN_CAR, vehicle_id);
        cv.put(CostsEntry.COLUMN_EXPENSE, price);
        cv.put(CostsEntry.COLUMN_TITLE, title);
        cv.put(CostsEntry.COLUMN_DETAILS, desc);
        cv.put(CostsEntry.COLUMN_ODO, odo);
        cv.put(CostsEntry.COLUMN_DATE, date);
        cv.put(CostsEntry.COLUMN_TYPE, type);

        db = getWritableDatabase();
        db.insert(CostsEntry.TABLE_NAME, null, cv);
    }

    public String getFirstCost(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MIN(" + CostsEntry.COLUMN_DATE + ") FROM " + CostsEntry.TABLE_NAME + " WHERE " + CostsEntry.COLUMN_CAR + " = " + vehicleID, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c.getString(0);
    }
    public String getLastCost(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT MAX(" + CostsEntry.COLUMN_DATE + ") FROM " + CostsEntry.TABLE_NAME + " WHERE " + CostsEntry.COLUMN_CAR + " = " + vehicleID, null);
        c.moveToFirst();
        if (c.isNull(0))
            return null;
        return c.getString(0);
    }

    public Cursor getAllCostsWhereTimeBetween(long vehicleID, long smallerTime, long biggerTime) {
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
        return c;
    }

    public Cursor getAllDrivesWhereTimeBetween(long vehicleID, long smallerTime, long biggerTime) {

        db = getReadableDatabase();
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
        return c;
    }
}

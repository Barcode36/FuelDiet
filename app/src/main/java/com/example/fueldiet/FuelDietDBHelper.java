package com.example.fueldiet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;

import com.example.fueldiet.FuelDietContract.*;


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
                DriveEntry.COLUMN_START_KM + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_TRIP_KM + " INTEGER NOT NULL, " +
                DriveEntry.COLUMN_CONSUMPTION + " REAL NOT NULL, " +
                DriveEntry.COLUMN_LITRES + " REAL NOT NULL, " +
                DriveEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + DriveEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";



        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_DRIVES_TABLE);

        createVehicles();
        createDrives();

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
                "(6, 'Abarth', '595 Competizione', '1.4L I4', 'Petrol', 'Manual', 180)");

    }

    private void createDrives() {
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_START_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_CONSUMPTION + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(1, 1563177015, 2, 650, 8.5, 55.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_START_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_CONSUMPTION + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(2, 1563516941, 652, 600, 9.0, 54.0, 2)");
        db.execSQL("INSERT INTO " + DriveEntry.TABLE_NAME + " (" + DriveEntry._ID + ", " +
                        DriveEntry.COLUMN_DATE + ", " + DriveEntry.COLUMN_START_KM + ", " +
                        DriveEntry.COLUMN_TRIP_KM + ", " + DriveEntry.COLUMN_CONSUMPTION + ", " +
                        DriveEntry.COLUMN_LITRES + ", " + DriveEntry.COLUMN_CAR + ") VALUES " +
                        "(3, 1563727966, 1252, 694, 7.2, 50.0, 2)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean resetDb() {
        db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
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
}

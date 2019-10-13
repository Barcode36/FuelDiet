package com.example.fueldiet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.fueldiet.FuelDietContract.*;


public class FuelDietDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fueldiet.db";
    public static final int DATABASE_VERSION = 1;

    public FuelDietDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

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
                DriveEntry.COLUMN_LITRES + "REAL NOT NULL, " +
                DriveEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + DriveEntry.COLUMN_CAR + ") REFERENCES " +
                VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";


        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_DRIVES_TABLE);

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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean resetDb() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        onCreate(db);
        return true;
    }
}

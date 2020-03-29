package com.fueldiet.fueldiet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.ReminderObject;
import com.fueldiet.fueldiet.object.VehicleObject;
import com.fueldiet.fueldiet.Utils;
import com.fueldiet.fueldiet.db.FuelDietContract.*;

import java.util.ArrayList;
import java.util.List;


public class FuelDietDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "fueldiet.db";
    public static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;

    /* create tables */

    private final String SQL_CREATE_VEHICLES_TABLE = "CREATE TABLE " +
            VehicleEntry.TABLE_NAME + "(" +
            VehicleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            VehicleEntry.COLUMN_MAKE + " TEXT NOT NULL, " +
            VehicleEntry.COLUMN_MODEL + " TEXT NOT NULL, " +
            VehicleEntry.COLUMN_ENGINE + " TEXT NOT NULL, " +
            VehicleEntry.COLUMN_FUEL_TYPE + " TEXT NOT NULL, " +
            VehicleEntry.COLUMN_HP + " INT NOT NULL, " +
            VehicleEntry.COLUMN_TORQUE + " INT NOT NULL, " +
            VehicleEntry.COLUMN_ODO_FUEL_KM + " INT NOT NULL DEFAULT 0, " +
            VehicleEntry.COLUMN_ODO_COST_KM + " INT NOT NULL DEFAULT 0, " +
            VehicleEntry.COLUMN_ODO_REMIND_KM + " INT NOT NULL DEFAULT 0, " +
            VehicleEntry.COLUMN_CUSTOM_IMG + " TEXT DEFAULT NULL, " +
            VehicleEntry.COLUMN_TRANSMISSION + " TEXT NOT NULL);";

    private final String SQL_CREATE_DRIVES_TABLE = "CREATE TABLE " +
            DriveEntry.TABLE_NAME + "(" +
            DriveEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DriveEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
            DriveEntry.COLUMN_ODO + " INTEGER NOT NULL, " +
            DriveEntry.COLUMN_TRIP + " INTEGER NOT NULL, " +
            DriveEntry.COLUMN_PRICE_LITRE + " REAL NOT NULL, " +
            DriveEntry.COLUMN_LITRES + " REAL NOT NULL, " +
            DriveEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
            DriveEntry.COLUMN_FIRST + " INTEGER NOT NULL DEFAULT 0, " +
            DriveEntry.COLUMN_NOT_FULL + " INTEGER NOT NULL DEFAULT 0, " +
            DriveEntry.COLUMN_NOTE + " TEXT DEFAULT NULL, " +
            DriveEntry.COLUMN_COUNTRY + " TEXT DEFAULT 'SI' NOT NULL, " +
            DriveEntry.COLUMN_LATITUDE + " REAL DEFAULT NULL, " +
            DriveEntry.COLUMN_LONGITUDE + " REAL DEFAULT NULL, " +
            DriveEntry.COLUMN_PETROL_STATION + " TEXT NOT NULL DEFAULT 'Other', " +
            "FOREIGN KEY (" + DriveEntry.COLUMN_CAR + ") REFERENCES " +
            VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

    private final String SQL_CREATE_COSTS_TABLE = "CREATE TABLE " +
            CostsEntry.TABLE_NAME + "(" +
            CostsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CostsEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
            CostsEntry.COLUMN_ODO + " INTEGER NOT NULL, " +
            CostsEntry.COLUMN_PRICE + " REAL NOT NULL, " +
            CostsEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
            CostsEntry.COLUMN_DETAILS + " TEXT, " +
            CostsEntry.COLUMN_TITLE + " TEXT NOT NULL, "  +
            CostsEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
            CostsEntry.COLUMN_RESET_KM + " INTEGER NOT NULL DEFAULT 0, "+
            "FOREIGN KEY (" + CostsEntry.COLUMN_CAR + ") REFERENCES " +
            VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

    private final String SQL_CREATE_REMINDERS_TABLE = "CREATE TABLE " +
            ReminderEntry.TABLE_NAME + "(" +
            ReminderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ReminderEntry.COLUMN_DATE + " INTEGER, " +
            ReminderEntry.COLUMN_ODO + " INTEGER, " +
            ReminderEntry.COLUMN_REPEAT + " INTEGER DEFAULT 0, " +
            ReminderEntry.COLUMN_CAR + " INTEGER NOT NULL, " +
            ReminderEntry.COLUMN_DETAILS + " TEXT, " +
            ReminderEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + ReminderEntry.COLUMN_CAR + ") REFERENCES " +
            VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + "));";

    private final String SQL_CREATE_PETROL_STATIONS_TABLE = "CREATE TABLE " +
            PetrolStationEntry.COLUMN_NAME + "(" +
            PetrolStationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PetrolStationEntry.COLUMN_NAME + " TEXT NOT NULL, " +
            PetrolStationEntry.COLUMN_FILE_NAME + " TEXT NOT NULL, " +
            PetrolStationEntry.COLUMN_ORIGIN + " INTEGER NOT NULL DEFAULT 1);";
    //origin = 1 means user added, 0 means developer added


    public FuelDietDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        this.db = db;

        initCreateTables();
    }

    private void initCreateTables() {
        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_DRIVES_TABLE);
        db.execSQL(SQL_CREATE_COSTS_TABLE);
        db.execSQL(SQL_CREATE_REMINDERS_TABLE);
        db.execSQL(SQL_CREATE_PETROL_STATIONS_TABLE);
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
        switch (oldVersion) {
            case 1:
                db.execSQL(SQL_CREATE_PETROL_STATIONS_TABLE);
        }
    }

    public boolean resetDb() {
        db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VehicleEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DriveEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CostsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReminderEntry.TABLE_NAME);
        initCreateTables();
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
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(DriveEntry._ID)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE))
            ));
        }
        c.close();
        return drives;
    }

    public DriveObject getPrevDrive(long id) {
        db = getReadableDatabase();
        //iskati največji datum?
        Cursor c = db.rawQuery("SELECT * FROM " + DriveEntry.TABLE_NAME + " WHERE " +
                DriveEntry.COLUMN_CAR + " = " + id + " AND " + DriveEntry.COLUMN_ODO + " = " +
                " ( SELECT MAX(" + DriveEntry.COLUMN_DATE + ") FROM " + DriveEntry.TABLE_NAME +
                " WHERE " + DriveEntry.COLUMN_CAR + " = " + id + ")", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE)));
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
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE)));
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
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE)));
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
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE)));
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
        DriveObject dv = new DriveObject(c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                c.getLong(c.getColumnIndex(DriveEntry._ID)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE)));
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
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_ODO)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_TRIP)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(DriveEntry._ID)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_PETROL_STATION)),
                    c.getString(c.getColumnIndex(DriveEntry.COLUMN_COUNTRY)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_FIRST)),
                    c.getInt(c.getColumnIndex(DriveEntry.COLUMN_NOT_FULL)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LATITUDE)),
                    c.getDouble(c.getColumnIndex(DriveEntry.COLUMN_LONGITUDE))
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

    public void removeLatestDrive(long vehicleID) {
        db = getWritableDatabase();
        db.delete(DriveEntry.TABLE_NAME,DriveEntry.COLUMN_CAR + " = " + vehicleID +
                " AND " + DriveEntry.COLUMN_DATE + " = (SELECT MAX(" + DriveEntry.COLUMN_DATE + ") FROM " + DriveEntry.TABLE_NAME +
                " WHERE " + DriveEntry.COLUMN_CAR + " = " + vehicleID +")", null);

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

    public ReminderObject getLatestDoneReminder(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + ReminderEntry.TABLE_NAME + " WHERE " +
                ReminderEntry.COLUMN_CAR + " = " + vehicleID + " AND " + ReminderEntry.COLUMN_DATE +
                " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL ORDER BY " +
                ReminderEntry.COLUMN_DATE + " DESC LIMIT 1 OFFSET 0", null);
        c.moveToFirst();
        if (c.getCount() == 0)
            return null;
        return Utils.getReminderObjectFromCursor(c, false).get(0);
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

    public List<CostObject> getAllCostWithReset(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CostsEntry.TABLE_NAME + " WHERE " +
                CostsEntry.COLUMN_CAR + " = " + vehicleID + " AND " +
                CostsEntry.COLUMN_RESET_KM + " = " + 1 + " ORDER BY " + CostsEntry.COLUMN_DATE + " DESC", null);

        return Utils.createCostObject(c);
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

    public List<ReminderObject> getAllActiveTimeReminders(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry.COLUMN_CAR + " = " +vehicleID + " AND " + ReminderEntry.COLUMN_DATE + " IS NOT NULL AND " + ReminderEntry.COLUMN_ODO + " IS NULL AND " + ReminderEntry.COLUMN_REPEAT + " = 0",
                null,
                null,
                null,
                ReminderEntry.COLUMN_DATE + " ASC"
        );
        return Utils.getReminderObjectFromCursor(c, true);
    }

    public List<ReminderObject> getAllActiveOdoReminders(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry.COLUMN_CAR + " = " +vehicleID + " AND " + ReminderEntry.COLUMN_DATE + " IS NULL AND " + ReminderEntry.COLUMN_ODO + " IS NOT NULL AND " + ReminderEntry.COLUMN_REPEAT + " = 0",
                null,
                null,
                null,
                ReminderEntry.COLUMN_ODO + " ASC"
        );
        return Utils.getReminderObjectFromCursor(c, true);
    }

    public List<ReminderObject> getAllActiveRepeatReminders(long vehicleID) {
        db = getReadableDatabase();
        Cursor c = db.query(
                ReminderEntry.TABLE_NAME,
                null,
                ReminderEntry.COLUMN_CAR + " = " +vehicleID + " AND (" + ReminderEntry.COLUMN_DATE + " IS NULL OR " + ReminderEntry.COLUMN_ODO + " IS NULL) AND " + ReminderEntry.COLUMN_REPEAT + " != 0",
                null,
                null,
                null,
                ReminderEntry.COLUMN_DATE + " ASC, " +ReminderEntry.COLUMN_ODO + " ASC"
        );
        return Utils.getReminderObjectFromCursor(c, true);
    }

    public List<ReminderObject> getAllDoneReminders(long vehicleID) {
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

    public int addReminder(long vehicle_id, String title, long date, String desc, int repeat) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderEntry.COLUMN_CAR, vehicle_id);
        cv.put(ReminderEntry.COLUMN_TITLE, title);
        cv.put(ReminderEntry.COLUMN_DETAILS, desc);
        cv.put(ReminderEntry.COLUMN_DATE, date);
        cv.put(ReminderEntry.COLUMN_REPEAT, repeat);

        db = getWritableDatabase();
        db.insert(ReminderEntry.TABLE_NAME, null, cv);
        Cursor c = db.rawQuery("SELECT MAX(" + ReminderEntry._ID + ") FROM " + ReminderEntry.TABLE_NAME + " WHERE " + ReminderEntry.COLUMN_CAR + " = " + vehicle_id, null);
        c.moveToFirst();
        return c.getInt(0);
    }
    public int addReminder(long vehicle_id, String title, int odo, String desc, int repeat) {
        ContentValues cv = new ContentValues();
        cv.put(ReminderEntry.COLUMN_CAR, vehicle_id);
        cv.put(ReminderEntry.COLUMN_TITLE, title);
        cv.put(ReminderEntry.COLUMN_DETAILS, desc);
        cv.put(ReminderEntry.COLUMN_ODO, odo);
        cv.put(ReminderEntry.COLUMN_REPEAT, repeat);

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

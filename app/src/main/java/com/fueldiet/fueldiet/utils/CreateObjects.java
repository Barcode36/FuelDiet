package com.fueldiet.fueldiet.utils;

import android.database.Cursor;

import com.fueldiet.fueldiet.db.FuelDietContract;
import com.fueldiet.fueldiet.object.CostObject;
import com.fueldiet.fueldiet.object.DriveObject;
import com.fueldiet.fueldiet.object.VehicleObject;

import java.util.ArrayList;
import java.util.List;

public class CreateObjects {
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
                    c.getLong(0),
                    c.getInt(8))
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
            /* check for null values */
            Double lat = null;
            Double lon = null;
            if (!c.isNull(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LONGITUDE))) {
                lat = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LATITUDE));
                lon = c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LONGITUDE));
            }

            driveObjects.add(new DriveObject(
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_ODO)),
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_TRIP)),
                    c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_LITRES)),
                    c.getDouble(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_DATE)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_CAR)),
                    c.getLong(c.getColumnIndex(FuelDietContract.DriveEntry._ID)),
                    c.getString(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOTE)),
                    c.getString(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_PETROL_STATION)),
                    c.getString(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_COUNTRY)),
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_FIRST)),
                    c.getInt(c.getColumnIndex(FuelDietContract.DriveEntry.COLUMN_NOT_FULL)),
                    lat,
                    lon
            ));
            pos++;
        }
        c.close();
        return driveObjects;
    }

    public static List<VehicleObject> createVehicleObjects(Cursor c) {
        List<VehicleObject> vehicleObjects = new ArrayList<>();

        int pos = 0;
        while (c.moveToPosition(pos)) {
            vehicleObjects.add(new VehicleObject(
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MAKE)),
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MODEL)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ENGINE)),
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE)),
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HYBRID_TYPE)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_MODEL_YEAR)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_HP)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_TORQUE)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_FUEL_KM)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_COST_KM)),
                    c.getInt(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_ODO_REMIND_KM)),
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION)),
                    c.getLong(c.getColumnIndex(FuelDietContract.VehicleEntry._ID)),
                    c.getString(c.getColumnIndex(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG))
            ));
            pos++;
        }
        c.close();
        return vehicleObjects;
    }
}

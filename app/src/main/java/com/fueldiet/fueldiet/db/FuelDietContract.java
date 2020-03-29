package com.fueldiet.fueldiet.db;

import android.provider.BaseColumns;

public class FuelDietContract {

    private FuelDietContract() {}

    public static class VehicleEntry implements BaseColumns {
        public static final String TABLE_NAME = "vehicles";
        public static final String COLUMN_MAKE = "make";
        public static final String COLUMN_MODEL = "model";
        public static final String COLUMN_FUEL_TYPE = "fueltype";
        public static final String COLUMN_ENGINE = "engine";
        public static final String COLUMN_TRANSMISSION = "transmission";
        public static final String COLUMN_HP = "hp";
        public static final String COLUMN_TORQUE = "torque";
        public static final String COLUMN_ODO_FUEL_KM = "odoFuelKm";
        public static final String COLUMN_ODO_COST_KM = "odoCostKm";
        public static final String COLUMN_ODO_REMIND_KM = "odoRemindKm";
        public static final String COLUMN_CUSTOM_IMG = "img";
    }

    public static class DriveEntry implements BaseColumns {
        public static final String TABLE_NAME = "drives";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ODO = "odo";
        public static final String COLUMN_TRIP = "trip";
        public static final String COLUMN_LITRES = "litres";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_PRICE_LITRE = "price";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_FIRST = "first";
        public static final String COLUMN_NOT_FULL = "notFull";
        public static final String COLUMN_PETROL_STATION = "petrolStation";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
    }

    public static class CostsEntry implements BaseColumns {
        public static final String TABLE_NAME = "costs";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_ODO = "odo";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RESET_KM = "resetKm";
        public static final String COLUMN_TYPE = "type";
    }

    public static class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_REPEAT = "repeat";
        public static final String COLUMN_ODO = "odo";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TITLE = "title";
    }

    public static class PetrolStationEntry implements BaseColumns {
        public static final String TABLE_NAME = "petrolStation";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_FILE_NAME = "fileName";
        public static final String COLUMN_ORIGIN = "origin";
    }
}

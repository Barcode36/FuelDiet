package com.example.fueldiet.db;

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
        public static final String COLUMN_INIT_KM = "newkm";
        public static final String COLUMN_CUSTOM_IMG = "img";
    }

    public static class DriveEntry implements BaseColumns {
        public static final String TABLE_NAME = "drives";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ODO_KM = "odoKM";
        public static final String COLUMN_TRIP_KM = "tripKM";
        public static final String COLUMN_LITRES = "litres";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_PRICE_LITRE = "price";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_FIRST = "first";
        public static final String COLUMN_NOT_FULL = "notFull";
        public static final String COLUMN_PETROL_STATION = "petrolStation";
    }

    public static class CostsEntry implements BaseColumns {
        public static final String TABLE_NAME = "costs";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_EXPENSE = "expense";
        public static final String COLUMN_ODO = "kilometres";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TYPE = "type";
    }

    public static class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ODO = "kilometres";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TITLE = "title";
    }
}

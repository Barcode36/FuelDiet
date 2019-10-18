package com.example.fueldiet;

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

    }

    public static class DriveEntry implements BaseColumns {
        public static final String TABLE_NAME = "drives";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ODO_KM = "odoKM";
        public static final String COLUMN_TRIP_KM = "tripKM";
        public static final String COLUMN_LITRES = "litres";
        public static final String COLUMN_CAR = "carID";
        public static final String COLUMN_PRICE_LITRE = "price";
    }
}

package com.example.fueldiet.object;

import android.content.ContentValues;

import com.example.fueldiet.db.FuelDietContract;

import java.util.Calendar;

public class DriveObject {

    private int odo;
    private int trip;
    private double litres;
    private double costPerLitre;
    private Calendar date;
    private long carID;
    private long id;

    public DriveObject() { }

    public DriveObject(int odo, int trip, double litres, double costPerLitre, long date, long carID, long id) {
        this.odo = odo;
        this.trip = trip;
        this.litres = litres;
        this.costPerLitre = costPerLitre;
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.carID = carID;
        this.id = id;
    }
    public DriveObject(int odo, int trip, double litres, double costPerLitre, long date, long carID) {
        this.odo = odo;
        this.trip = trip;
        this.litres = litres;
        this.costPerLitre = costPerLitre;
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.carID = carID;
    }

    public int getOdo() {
        return odo;
    }

    public void setOdo(int odo) {
        this.odo = odo;
    }

    public boolean setOdo(String odo) {
        if (odo.equals(""))
            return false;
        this.odo = Integer.parseInt(odo);
        return true;
    }

    public int getTrip() {
        return trip;
    }

    public void setTrip(int trip) {
        this.trip = trip;
    }

    public boolean setTrip(String trip) {
        if (trip.equals(""))
            return false;
        this.trip = Integer.parseInt(trip);
        return true;
    }

    public double getLitres() {
        return litres;
    }

    public void setLitres(double litres) {
        this.litres = litres;
    }

    public boolean setLitres(String litres) {
        if (litres.equals(""))
            return false;
        this.litres = Double.parseDouble(litres);
        return true;
    }

    public double getCostPerLitre() {
        return costPerLitre;
    }

    public void setCostPerLitre(double costPerLitre) {
        this.costPerLitre = costPerLitre;
    }

    public boolean setCostPerLitre(String costPerLitre) {
        if (costPerLitre.equals(""))
            return false;
        this.costPerLitre = Double.parseDouble(costPerLitre);
        return true;
    }

    public Calendar getDate() {
        return date;
    }

    public boolean setDate(Calendar date) {
        this.date = date;
        return true;
    }

    public long getCarID() {
        return carID;
    }

    public boolean setCarID(long carID) {
        this.carID = carID;
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDateEpoch() {
        return date.getTimeInMillis()/1000;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.DriveEntry.COLUMN_CAR, this.getCarID());
        cv.put(FuelDietContract.DriveEntry.COLUMN_DATE, this.getDateEpoch());
        cv.put(FuelDietContract.DriveEntry.COLUMN_ODO_KM, this.getOdo());
        cv.put(FuelDietContract.DriveEntry.COLUMN_TRIP_KM, this.getTrip());
        cv.put(FuelDietContract.DriveEntry.COLUMN_LITRES, this.getLitres());
        cv.put(FuelDietContract.DriveEntry.COLUMN_PRICE_LITRE, this.getCostPerLitre());
        return cv;
    }
}

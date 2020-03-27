package com.fueldiet.fueldiet.object;

import android.content.ContentValues;

import com.fueldiet.fueldiet.db.FuelDietContract.CostsEntry;

import java.util.Calendar;

public class CostObject {

    private long carID;
    private Calendar date;
    private double cost;
    private int km;
    private String details;
    private String title;
    private String type;
    private long costID;
    private int resetKm;

    public CostObject() {}

    public CostObject(long carID, long date, double cost, int km, String details, String title, String type, int resetKm) {
        this.carID = carID;
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.cost = cost;
        this.km = km;
        this.details = details;
        this.title = title;
        this.type = type;
        this.resetKm = resetKm;
    }

    public CostObject(long carID, long date, double cost, int km, String details, String title, String type, long costID, int resetKm) {
        this.carID = carID;
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        this.cost = cost;
        this.km = km;
        this.details = details;
        this.title = title;
        this.type = type;
        this.costID = costID;
        this.resetKm = resetKm;
    }

    public long getCarID() {
        return carID;
    }

    public void setCarID(long carID) {
        this.carID = carID;
    }

    public Calendar getDate() {
        return date;
    }

    public long getDateEpoch() {
        return date.getTimeInMillis()/1000;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public boolean setDate(long date) {
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(date*1000);
        return true;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public boolean setCost(String cost) {
        if (cost.equals(""))
            return false;
        this.cost = Double.parseDouble(cost);
        return true;
    }

    public int getKm() {
        return km;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public boolean setKm(String km) {
        if (km.equals(""))
            return false;
        this.km = Integer.parseInt(km);
        return true;
    }

    public String getDetails() {
        return details;
    }

    public boolean setDetails(String details) {
        if (details.equals(""))
            this.details = null;
        else
            this.details = details;
        return true;
    }

    public String getTitle() {
        return title;
    }

    public boolean setTitle(String title) {
        if (title.equals(""))
            return false;
        this.title = title;
        return true;
    }

    public String getType() {
        return type;
    }

    public boolean setType(String type) {
        if (type == null)
            return false;
        this.type = type;
        return true;
    }

    public int getResetKm() {
        return resetKm;
    }

    public void setResetKm(int resetKm) {
        this.resetKm = resetKm;
    }

    public long getCostID() {
        return costID;
    }

    public void setCostID(long costID) {
        this.costID = costID;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(CostsEntry.COLUMN_CAR, getCarID());
        cv.put(CostsEntry.COLUMN_PRICE, getCost());
        cv.put(CostsEntry.COLUMN_TITLE, getTitle());
        cv.put(CostsEntry.COLUMN_DETAILS, getDetails());
        cv.put(CostsEntry.COLUMN_ODO, getKm());
        cv.put(CostsEntry.COLUMN_DATE, getDateEpoch());
        cv.put(CostsEntry.COLUMN_TYPE, getType());
        cv.put(CostsEntry.COLUMN_RESET_KM, getResetKm());
        return cv;
    }
}

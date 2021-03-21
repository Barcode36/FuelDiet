package com.fueldiet.fueldiet.object;

import android.content.ContentValues;

import com.fueldiet.fueldiet.db.FuelDietContract.CostItemsEntry;

public class CostItemObject {

    private long costID;
    private long costItemID;
    private String description;
    private String name;
    private double price;

    public CostItemObject(long costID, long costItemID, String description, String name, double price) {
        this.costID = costID;
        this.costItemID = costItemID;
        this.description = description;
        this.name = name;
        this.price = price;
    }

    public CostItemObject(long costID, String description, String name, double price) {
        this.costID = costID;
        this.description = description;
        this.name = name;
        this.price = price;
    }

    public CostItemObject() {}

    public long getCostID() {
        return costID;
    }

    public void setCostID(long costID) {
        this.costID = costID;
    }

    public long getCostItemID() {
        return costItemID;
    }

    public void setCostItemID(long costItemID) {
        this.costItemID = costItemID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(CostItemsEntry.COLUMN_COST, getCostID());
        cv.put(CostItemsEntry.COLUMN_DESCRIPTION, getDescription());
        cv.put(CostItemsEntry.COLUMN_NAME, getName());
        cv.put(CostItemsEntry.COLUMN_PRICE, getPrice());
        return cv;
    }
}

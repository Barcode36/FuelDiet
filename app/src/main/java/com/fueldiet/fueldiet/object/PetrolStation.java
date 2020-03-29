package com.fueldiet.fueldiet.object;

import android.content.ContentValues;

import com.fueldiet.fueldiet.db.FuelDietContract;

public class PetrolStation {

    private long id;
    private String name;
    private String fileName;
    private int origin;

    public PetrolStation(long id, String name, String fileName, int origin) {
        this.id = id;
        this.name = name;
        this.fileName = fileName;
        this.origin = origin;
    }

    public PetrolStation(String name, String fileName, int origin) {
        this.name = name;
        this.fileName = fileName;
        this.origin = origin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.PetrolStationEntry.COLUMN_NAME, getName());
        cv.put(FuelDietContract.PetrolStationEntry.COLUMN_FILE_NAME, getFileName());
        cv.put(FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN, getOrigin());
        return cv;
    }
}

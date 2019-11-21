package com.example.fueldiet.Object;

import android.content.ContentValues;

import com.example.fueldiet.db.FuelDietContract;

import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleObject {

    private String make;
    private String model;
    private String engine;
    private String fuel;
    private int hp;
    private int initKM;
    private String transmission;
    private long id;
    private String customImg;

    public String getCustomImg() {
        return customImg;
    }

    public void setCustomImg(String customImg) {
        this.customImg = customImg;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public VehicleObject() {}

    public VehicleObject(String make, String model, String engine, String fuel, int hp, int initKM, String transmission, long id, String customImg) {
        this.make = make;
        this.model = model;
        this.engine = engine;
        this.fuel = fuel;
        this.hp = hp;
        this.initKM = initKM;
        this.transmission = transmission;
        this.id = id;
        this.customImg = customImg;
    }

    public String getMake() {
        return make;
    }

    public boolean setMake(String mBrand) {
        this.make = toCapitalCaseWords(mBrand);
        return !(mBrand.length() == 0);
    }

    public String getModel() {
        return model;
    }

    public boolean setModel(String mModel) {
        this.model = toCapitalCaseWords(mModel);
        return !(mModel.length() == 0);
    }

    public String getEngine() {
        return engine;
    }

    public boolean setEngine(String mEngine) {
        this.engine = mEngine;
        return !(mEngine.length() == 0);
    }

    public String getFuel() {
        return fuel;
    }

    public boolean setFuel(String mFuel) {
        this.fuel = toCapitalCaseWords(mFuel);
        return !(mFuel.length() == 0);
    }

    public int getHp() {
        return hp;
    }

    public boolean setHp(int mHp) {
        if ((mHp+"").length() == 0)
            return false;
        this.hp = mHp;
        return true;
    }
    public boolean setHp(String mHp) {
        if (mHp.length() == 0)
            return false;
        this.hp = Integer.parseInt(mHp);
        return true;
    }

    public int getInitKM() {
        return initKM;
    }

    public boolean setInitKM(int initKM) {
        this.initKM = initKM;
        return true;
    }

    public boolean setInitKM(String initKM) {
        this.initKM = Integer.parseInt(initKM);
        return true;
    }

    public String getTransmission() {
        return transmission;
    }

    public boolean setTransmission(String mTransmission) {
        this.transmission = toCapitalCaseWords(mTransmission);
        return !(mTransmission.length() == 0);
    }
    
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, this.getEngine());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, this.getFuel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, this.getHp());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, this.getMake());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, this.getModel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_INIT_KM, this.getInitKM());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, this.getTransmission());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG, this.getCustomImg());

        return cv;
    }
}


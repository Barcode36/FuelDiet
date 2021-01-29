package com.fueldiet.fueldiet.object;

import android.content.ContentValues;

import com.fueldiet.fueldiet.db.FuelDietContract;

import static com.fueldiet.fueldiet.Utils.toCapitalCaseWords;

public class VehicleObject {

    private long id;
    private String make;
    private String model;
    private String fuelType;
    private String hybridType;
    private double engine;
    private String transmission;
    private int modelYear;
    private int hp;
    private int torque;

    private int odoFuelKm;
    private int odoCostKm;
    private int odoRemindKm;

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

    public VehicleObject(String make, String model, long id) {
        this.make = make;
        this.model = model;
        this.id = id;
    }

    public VehicleObject(String make, String model, int engine, String fuelType, String hybridType,
                         int modelYear, int hp, int torque, int odoFuelKm, int odoCostKm,
                         int odoRemindKm, String transmission, long id, String customImg) {
        this.make = make;
        this.model = model;
        this.engine = engine;
        this.hybridType = hybridType;
        this.fuelType = fuelType;
        this.modelYear = modelYear;
        this.hp = hp;
        this.odoFuelKm = odoFuelKm;
        this.odoCostKm = odoCostKm;
        this.odoRemindKm = odoRemindKm;
        this.transmission = transmission;
        this.id = id;
        this.customImg = customImg;
        this.torque = torque;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String mBrand) {
        this.make = toCapitalCaseWords(mBrand);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String mModel) {
        this.model = toCapitalCaseWords(mModel);
    }

    public double getEngine() {
        return engine;
    }

    public void setEngine(double mEngine) {
        this.engine = mEngine;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String mFuel) {
        this.fuelType = toCapitalCaseWords(mFuel);
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

    public int getOdoFuelKm() {
        return odoFuelKm;
    }

    public void setOdoFuelKm(int odoFuelKm) {
        this.odoFuelKm = odoFuelKm;
    }

    public void setOdoFuelKm(String initKM) {
        this.odoFuelKm = Integer.parseInt(initKM);
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String mTransmission) {
        this.transmission = toCapitalCaseWords(mTransmission);
    }

    public int getOdoCostKm() {
        return odoCostKm;
    }

    public void setOdoCostKm(int odoCostKm) {
        this.odoCostKm = odoCostKm;
    }

    public int getOdoRemindKm() {
        return odoRemindKm;
    }

    public void setOdoRemindKm(int odoRemindKm) {
        this.odoRemindKm = odoRemindKm;
    }

    public int getTorque() {
        return torque;
    }

    public void setTorque(int torque) {
        this.torque = torque;
    }

    public String getHybridType() {
        return hybridType;
    }

    public void setHybridType(String hybridType) {
        this.hybridType = hybridType;
    }

    public int getModelYear() {
        return modelYear;
    }

    public void setModelYear(int modelYear) {
        this.modelYear = modelYear;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, this.getEngine());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, this.getFuelType());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, this.getHp());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TORQUE, this.getTorque());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, this.getMake());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, this.getModel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL_YEAR, this.getModelYear());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HYBRID_TYPE, this.getHybridType());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_FUEL_KM, this.getOdoFuelKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_COST_KM, this.getOdoCostKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_REMIND_KM, this.getOdoRemindKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, this.getTransmission());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG, this.getCustomImg());

        return cv;
    }
}


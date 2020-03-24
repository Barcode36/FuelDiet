package com.fueldiet.fueldiet.object;

import android.content.ContentValues;

import com.fueldiet.fueldiet.db.FuelDietContract;

import static com.fueldiet.fueldiet.Utils.toCapitalCaseWords;

public class VehicleObject {

    private String make;
    private String model;
    private String engine;
    private String fuel;
    private int hp;
    private int torque;
    private int odoFuelKm;
    private int odoCostKm;
    private int odoRemindKm;
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

    public VehicleObject(String make, String model, long id) {
        this.make = make;
        this.model = model;
        this.id = id;
    }

    public VehicleObject(String make, String model, String engine, String fuel, int hp, int torque, int odoFuelKm, int odoCostKm, int odoRemindKm, String transmission, long id, String customImg) {
        this.make = make;
        this.model = model;
        this.engine = engine;
        this.fuel = fuel;
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

    public int getOdoFuelKm() {
        return odoFuelKm;
    }

    public void setOdoFuelKm(int odoFuelKm) {
        this.odoFuelKm = odoFuelKm;
    }

    public boolean setOdoFuelKm(String initKM) {
        this.odoFuelKm = Integer.parseInt(initKM);
        return true;
    }

    public String getTransmission() {
        return transmission;
    }

    public boolean setTransmission(String mTransmission) {
        this.transmission = toCapitalCaseWords(mTransmission);
        return !(mTransmission.length() == 0);
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

    public boolean setTorque(String mTorque) {
        if (mTorque.length() == 0)
            return false;
        this.torque = Integer.parseInt(mTorque);
        return true;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, this.getEngine());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, this.getFuel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, this.getHp());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TORQUE, this.getTorque());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, this.getMake());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, this.getModel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_FUEL_KM, this.getOdoFuelKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_COST_KM, this.getOdoCostKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ODO_REMIND_KM, this.getOdoRemindKm());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, this.getTransmission());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_CUSTOM_IMG, this.getCustomImg());

        return cv;
    }
}


package com.example.fueldiet;

import android.content.ContentValues;
import android.widget.ImageView;

import static com.example.fueldiet.MainActivity.LOGO_URL;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleObject {

    private String mBrand;
    private String mModel;
    private String mEngine;
    private String mFuel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private int mHp;
    private String mTransmission;
    private long id;

    public VehicleObject() {}

    public String getmBrand() {
        return mBrand;
    }

    public boolean setmBrand(String mBrand) {
        this.mBrand = toCapitalCaseWords(mBrand);
        return !(mBrand.length() == 0);
    }

    public String getmModel() {
        return mModel;
    }

    public boolean setmModel(String mModel) {
        this.mModel = toCapitalCaseWords(mModel);
        return !(mModel.length() == 0);
    }

    public String getmEngine() {
        return mEngine;
    }

    public boolean setmEngine(String mEngine) {
        this.mEngine = mEngine;
        return !(mEngine.length() == 0);
    }

    public String getmFuel() {
        return mFuel;
    }

    public boolean setmFuel(String mFuel) {
        this.mFuel = toCapitalCaseWords(mFuel);
        return !(mFuel.length() == 0);
    }

    public int getmHp() {
        return mHp;
    }

    public boolean setmHp(int mHp) {
        if ((mHp+"").length() == 0)
            return false;
        this.mHp = mHp;
        return true;
    }
    public boolean setmHp(String mHp) {
        if (mHp.length() == 0)
            return false;
        this.mHp = Integer.parseInt(mHp);
        return true;
    }

    public String getmTransmission() {
        return mTransmission;
    }

    public boolean setmTransmission(String mTransmission) {
        this.mTransmission = toCapitalCaseWords(mTransmission);
        return !(mTransmission.length() == 0);
    }
    
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.VehicleEntry.COLUMN_ENGINE, this.getmEngine());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_FUEL_TYPE, this.getmFuel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_HP, this.getmHp());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MAKE, this.getmBrand());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_MODEL, this.getmModel());
        cv.put(FuelDietContract.VehicleEntry.COLUMN_TRANSMISSION, this.getmTransmission());

        return cv;
    }
}


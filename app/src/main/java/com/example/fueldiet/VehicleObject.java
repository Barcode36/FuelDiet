package com.example.fueldiet;

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

    public void setmBrand(String mBrand) {
        this.mBrand = toCapitalCaseWords(mBrand);
    }

    public String getmModel() {
        return mModel;
    }

    public void setmModel(String mModel) {
        this.mModel = toCapitalCaseWords(mModel);
    }

    public String getmEngine() {
        return mEngine;
    }

    public void setmEngine(String mEngine) {
        this.mEngine = mEngine;
    }

    public String getmFuel() {
        return mFuel;
    }

    public void setmFuel(String mFuel) {
        this.mFuel = toCapitalCaseWords(mFuel);
    }

    public int getmHp() {
        return mHp;
    }

    public void setmHp(int mHp) {
        this.mHp = mHp;
    }

    public String getmTransmission() {
        return mTransmission;
    }

    public void setmTransmission(String mTransmission) {
        this.mTransmission = toCapitalCaseWords(mTransmission);
    }
}


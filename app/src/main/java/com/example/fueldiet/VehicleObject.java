package com.example.fueldiet;

import android.widget.ImageView;

import static com.example.fueldiet.MainActivity.LOGO_URL;
import static com.example.fueldiet.Utils.toCapitalCaseWords;

public class VehicleObject {

    private String mVehicleBrand;
    private String mVehicleModel;
    private String mVehicleData;
    private String imageURL;

    public VehicleObject(String brand, String model, String data) {
        mVehicleBrand = brand;
        mVehicleModel = model;
        mVehicleData = data;

        imageURL = String.format(LOGO_URL, toCapitalCaseWords(mVehicleBrand));
    }

    public void changeText1(String text) {
        mVehicleData = text;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getmVehicleBrand() {
        return mVehicleBrand;
    }

    public String getmVehicleModel() {
        return mVehicleModel;
    }

    public String getmVehicleData() {
        return mVehicleData;
    }
}


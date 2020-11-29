package com.fueldiet.fueldiet.object;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import com.fueldiet.fueldiet.db.FuelDietContract;
import com.fueldiet.fueldiet.db.FuelDietDBHelper;

import java.io.ByteArrayOutputStream;

public class PetrolStationObject {

    private long id;
    private String name;
    private int origin;
    private String fileName;
    private Bitmap logo;

    public PetrolStationObject(long id, String name, int origin) {
        this.id = id;
        this.name = name;
        this.origin = origin;
        this.fileName = name.toLowerCase().replaceAll(" ", "__").concat(".png");
        this.logo = null;
    }

    public PetrolStationObject(String name, int origin) {
        this.name = name;
        this.origin = origin;
        this.fileName = name.toLowerCase().replaceAll(" ", "__").concat(".png");
        this.logo = null;
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

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Bitmap getLogo(Context context) {
        FuelDietDBHelper dbHelper = FuelDietDBHelper.getInstance(context);
        return dbHelper.getPetrolStationImage(id);
    }

    public void setLogo(Bitmap bitmap) {
        this.logo = bitmap;
    }

    private byte[] getByteLogo() {
        if (logo != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            logo.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();
        }
        return null;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.PetrolStationEntry.COLUMN_NAME, getName());
        cv.put(FuelDietContract.PetrolStationEntry.COLUMN_ORIGIN, getOrigin());
        if (this.origin == 1 && this.logo != null) {
            cv.put(FuelDietContract.PetrolStationEntry.COLUMN_LOGO, getByteLogo());
        } else if (this.origin == 0) {
            cv.putNull(FuelDietContract.PetrolStationEntry.COLUMN_LOGO);
        }
        return cv;
    }
}

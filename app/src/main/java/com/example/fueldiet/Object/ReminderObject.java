package com.example.fueldiet.Object;

import android.content.ContentValues;
import com.example.fueldiet.db.FuelDietContract;
import java.util.Date;

public class ReminderObject {

    private Date date;
    private Integer km;
    private String title;
    private String desc;
    private boolean active;
    private int id;
    private long carID;

    public ReminderObject(int id) {
        this.id = id;
    }

    public ReminderObject(int id, long date, int km, String title, String desc, boolean active, long carID) {
        if (date == 0) {
            this.km = km;
            this.date = null;
        } else if (date > 0 && km == 0) {
            this.date = new Date(date*1000);
            this.km = null;
        } else {
            this.date = new Date(date*1000);
            this.km = km;
        }
        this.id = id;
        this.carID = carID;
        this.title = title;
        this.desc = desc;
        this.active = active;
    }

    public long getCarID() {
        return carID;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public long getDateEpoch() {
        return date.getTime()/1000;
    }

    public Integer getKm() {
        return km;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isActive() {
        return active;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(FuelDietContract.ReminderEntry.COLUMN_CAR, this.getCarID());
        cv.put(FuelDietContract.ReminderEntry.COLUMN_DATE, this.getDateEpoch());
        cv.put(FuelDietContract.ReminderEntry.COLUMN_DETAILS, this.getDesc());
        cv.put(FuelDietContract.ReminderEntry.COLUMN_ODO, this.getKm());
        cv.put(FuelDietContract.ReminderEntry.COLUMN_TITLE, this.getTitle());
        return cv;
    }
}

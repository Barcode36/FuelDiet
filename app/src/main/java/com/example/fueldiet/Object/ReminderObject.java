package com.example.fueldiet.Object;

import java.util.Calendar;
import java.util.Date;

public class ReminderObject {

    private Date date;
    private Integer km;
    private String title;
    private String desc;
    private boolean active;
    private int id;

    public ReminderObject(int id) {
        this.id = id;
    }

    public ReminderObject(int id, long date, int km, String title, String desc, boolean active) {
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
        this.title = title;
        this.desc = desc;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
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
}

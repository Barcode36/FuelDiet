package com.example.fueldiet.Object;

import com.example.fueldiet.Activity.MainActivity;


public class ManufacturerObject {

    private String name;
    private String fileName;
    private String url;

    public ManufacturerObject(String name, final String fileName, String url_addr) {
        this.name = name;
        this.fileName = fileName;
        this.url = String.format(MainActivity.LOGO_URL, fileName);
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() { return this.url; }

}

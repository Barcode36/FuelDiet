package com.example.fueldiet;

public class ManufacturerObject {

    private String name;
    private String fileName;
    private String url_addr;

    public ManufacturerObject(String name, String fileName, String url_addr) {
        this.name = name;
        this.fileName = fileName;
        this.url_addr = url_addr;
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

    public String getUrl_addr() {
        return url_addr;
    }

    public void setUrl_addr(String url_addr) {
        this.url_addr = url_addr;
    }
}

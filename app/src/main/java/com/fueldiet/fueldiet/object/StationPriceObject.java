package com.fueldiet.fueldiet.object;

import java.util.HashMap;

public class StationPriceObject {

    private String name;
    private String address;
    private int franchise;
    private double lat;
    private double lng;
    private String direction;
    private HashMap<String, Double> prices;

    public StationPriceObject(String name, String address, int franchise, double lat, double lon, String direction) {
        this.name = name;
        this.address = address;
        this.franchise = franchise;
        this.lat = lat;
        this.lng = lon;
        this.direction = direction;
    }

    public void setPrices(HashMap<String, Double> prices) {
        this.prices = prices;
    }

    public int getFranchise() {
        return franchise;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lng;
    }

    public String getDirection() {
        return direction;
    }
}

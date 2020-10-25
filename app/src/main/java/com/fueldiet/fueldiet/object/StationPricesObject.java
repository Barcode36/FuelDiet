package com.fueldiet.fueldiet.object;

import java.io.Serializable;
import java.util.HashMap;

public class StationPricesObject implements Serializable {

    private int pk;
    private String name;
    private String address;
    private Integer franchise;
    private Double lat;
    private Double lng;
    private String direction;
    private HashMap<String, Double> prices;

    public StationPricesObject(String name, String address, int franchise, double lat, double lon, String direction) {
        this.name = name;
        this.address = address;
        this.franchise = franchise;
        this.lat = lat;
        this.lng = lon;
        this.direction = direction;
    }

    public StationPricesObject(String address) {
        this.address = address;
        this.name = null;
        this.franchise = null;
        this.lat = null;
        this.lng = null;
        this.direction = null;
    }

    public int getPk() {
        return pk;
    }

    public void setPrices(HashMap<String, Double> prices) {
        this.prices = prices;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFranchise() {
        return franchise;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lng;
    }

    public String getDirection() {
        return direction;
    }

    public HashMap<String, Double> getPrices() {
        return prices;
    }
}

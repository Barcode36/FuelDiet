package com.fueldiet.fueldiet.object;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.HashMap;

public class StationPricesObject implements Serializable {

    private int pk;
    private String name;
    private String address;
    private Integer franchise;
    private Double lat;
    private Double lng;
    private Double distance;
    private HashMap<String, Double> prices;

    public StationPricesObject() {
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

    public Double getLng() {
        return lng;
    }

    public Double getDistance() {
        return distance;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    public Point getPoint() {
        return Point.fromLngLat(lng, lat);
    }

    public HashMap<String, Double> getPrices() {
        return prices;
    }
}

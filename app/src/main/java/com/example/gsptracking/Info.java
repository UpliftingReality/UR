package com.example.gsptracking;

public class Info {
    private double lat;
    private double lon;

    public Info(double _lat, double _lon) {
        this.lat = _lat;
        this.lon = _lon;
    }

    public void set(double _lat, double _lon) {
        this.lat = _lat;
        this.lon = _lon;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }
}

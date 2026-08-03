package com.example.team20_tim_djordjina.model;

/** An ordered stop, used in the ride request and echoed in the response. */
public class RideStopRequest {

    private String address;
    private double latitude;
    private double longitude;
    private int stopOrder;

    public RideStopRequest(String address, double latitude, double longitude, int stopOrder) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopOrder = stopOrder;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getStopOrder() {
        return stopOrder;
    }
}

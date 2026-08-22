package com.example.team20_tim_djordjina.model;

/** Body for stopping a ride -> where the car stopped (US#2.6.5) */
public class StopRideRequest {
    private double latitude;
    private double longitude;

    public StopRideRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

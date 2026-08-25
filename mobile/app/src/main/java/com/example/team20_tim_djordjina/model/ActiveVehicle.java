package com.example.team20_tim_djordjina.model;

/** An active vehicle for the public home map (US#2.1.1). Mirrors ActiveVehicleDTO */
public class ActiveVehicle {
    private Long vehicleId;
    private double latitude;
    private double longitude;
    private boolean busy;
    private String vehicleType;
    private String model;

    public Long getVehicleId() {
        return vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isBusy() {
        return busy;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getModel() {
        return model;
    }
}

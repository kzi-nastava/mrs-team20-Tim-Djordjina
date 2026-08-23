package com.example.team20_tim_djordjina.model;

public class Tracking {
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private long etaMinutes;
    private String status;
    private double pickupLatitude;
    private double pickupLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private String driverName;
    private String vehicleModel;
    private String licensePlate;

    public Double getVehicleLatitude() {
        return vehicleLatitude;
    }

    public Double getVehicleLongitude() {
        return vehicleLongitude;
    }

    public long getEtaMinutes() {
        return etaMinutes;
    }

    public String getStatus() {
        return status;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
}

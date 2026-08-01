package com.example.team20_tim_djordjina.model;

import java.util.List;

/** Response from POST /api/rides. Mirrors the backend RideResponseDTO */
public class RideResponse {

    private Long id;
    private String status;
    private double distanceKm;
    private double fare;
    private String pickupAddress;
    private String destinationAddress;
    private List<RideStopRequest> stops;
    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;
    private DriverInfo driver;
    private String message;

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public List<RideStopRequest> getStops() {
        return stops;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public boolean isBabyTransport() {
        return babyTransport;
    }

    public boolean isPetTransport() {
        return petTransport;
    }

    public DriverInfo getDriver() {
        return driver;
    }

    public String getMessage() {
        return message;
    }

    public static class DriverInfo {
        private Long driverId;
        private String firstName;
        private String lastName;
        private String vehicleModel;
        private String licensePlate;

        public Long getDriverId() {
            return driverId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getVehicleModel() {
            return vehicleModel;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public String getFullName(){
            return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
        }
    }
}

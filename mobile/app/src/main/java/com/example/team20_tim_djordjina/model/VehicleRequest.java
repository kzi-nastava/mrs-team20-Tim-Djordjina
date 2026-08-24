package com.example.team20_tim_djordjina.model;

/* Vehicle part of the admin driver-registration request
*  Mirrors the backend VehicleDTO */
public class VehicleRequest {
    private String model;
    private String vehicleType;
    private String licensePlate;
    private int seats;
    private boolean babyTransport;
    private boolean petTransport;

    public VehicleRequest(String model, String vehicleType, String licensePlate, int seats, boolean babyTransport, boolean petTransport) {
        this.model = model;
        this.vehicleType = vehicleType;
        this.licensePlate = licensePlate;
        this.seats = seats;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public boolean isBabyTransport() {
        return babyTransport;
    }

    public void setBabyTransport(boolean babyTransport) {
        this.babyTransport = babyTransport;
    }

    public boolean isPetTransport() {
        return petTransport;
    }

    public void setPetTransport(boolean petTransport) {
        this.petTransport = petTransport;
    }
}

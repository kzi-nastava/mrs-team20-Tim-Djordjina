package com.example.team20_tim_djordjina.model;

public class Rating {

    private Long id;
    private Long rideId;
    private Integer driverRating;
    private Integer vehicleRating;
    private String comment;

    public Rating () {}

    public Rating (Integer driverRating, Integer vehicleRating, String comment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public Integer getDriverRating() {
        return driverRating;
    }

    public Integer getVehicleRating() {
        return vehicleRating;
    }

    public String getComment() {
        return comment;
    }
}

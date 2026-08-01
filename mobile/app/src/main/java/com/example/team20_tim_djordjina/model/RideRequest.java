package com.example.team20_tim_djordjina.model;

import java.util.List;

/** Body for POST /api/rides. Mirrors the backend RideRequestDTO */
public class RideRequest {

    private String pickupAddress;
    private String pickupLatitude;
    private String pickupLongitude;

    private String destinationAddress;
    private String destinationLatitude;
    private String destinationLongitude;

    private List<RideStopRequest> stops;

    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    public RideRequest(String pickupAddress, String pickupLatitude, String pickupLongitude,
                       String destinationAddress, String destinationLatitude,
                       String destinationLongitude, List<RideStopRequest> stops,
                       String vehicleType, boolean babyTransport, boolean petTransport) {
        this.pickupAddress = pickupAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.destinationAddress = destinationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.stops = stops;
        this.vehicleType = vehicleType;
        this.babyTransport = babyTransport;
        this.petTransport = petTransport;
    }
}

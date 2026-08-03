package com.example.team20_tim_djordjina.model;

import java.util.List;

/** Body for POST /api/rides. Mirrors the backend RideRequestDTO */
public class RideRequest {

    private String pickupAddress;
    private double pickupLatitude;
    private double pickupLongitude;

    private String destinationAddress;
    private double destinationLatitude;
    private double destinationLongitude;

    private List<RideStopRequest> stops;

    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    public RideRequest(String pickupAddress, double pickupLatitude, double pickupLongitude,
                       String destinationAddress, double destinationLatitude,
                       double destinationLongitude, List<RideStopRequest> stops,
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

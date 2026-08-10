package com.example.team20_tim_djordjina.model;

import java.util.List;

/**
 * A saved route. Used both as the POST body and the GET response
 * Mirrors the backend FavouriteRouteDTO
 * */
public class FavouriteRoute {

    private Long id;
    private String label;
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private List<RideStopRequest> stops;

    public FavouriteRoute() {}

    public FavouriteRoute(Long id, String label,
                          String pickupAddress, Double pickupLatitude, Double pickupLongitude,
                          String destinationAddress, Double destinationLatitude, Double destinationLongitude,
                          List<RideStopRequest> stops) {
        this.id = id;
        this.label = label;
        this.pickupAddress = pickupAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.destinationAddress = destinationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.stops = stops;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public List<RideStopRequest> getStops() {
        return stops;
    }
}

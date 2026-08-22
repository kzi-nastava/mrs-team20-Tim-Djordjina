package com.example.team20_tim_djordjina.model;

/** Body for cancelling a ride */
public class CancelRideRequest {
    private String reason;

    public CancelRideRequest(String reason) {
        this.reason = reason;
    }
}

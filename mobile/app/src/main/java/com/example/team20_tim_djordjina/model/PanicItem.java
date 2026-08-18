package com.example.team20_tim_djordjina.model;

/**
 *  A panic record for the admin list.
 *  Mirrors the backend PanicDTO.
 *  */
public class PanicItem {
    private Long id;
    private Long rideId;
    private String rideStatus;
    private String pickupAddress;
    private String destinationAddress;
    private String triggeredByName;
    private String triggeredByEmail;
    private String triggeredByRole;
    private String note;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public Long getRideId() {
        return rideId;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public String getTriggeredByName() {
        return triggeredByName;
    }

    public String getTriggeredByEmail() {
        return triggeredByEmail;
    }

    public String getTriggeredByRole() {
        return triggeredByRole;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDisplayDate() {
        if (createdAt == null) return "";
        String s = createdAt.replace('T', ' ');
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}

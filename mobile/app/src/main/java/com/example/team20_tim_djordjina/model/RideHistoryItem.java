package com.example.team20_tim_djordjina.model;

import java.util.List;

public class RideHistoryItem {

    private Long id;
    private String status;
    private String pickupAddress;
    private String destinationAddress;
    private List<RideStopRequest> stops;
    private double distanceKm;
    private double fare;
    private String vehicleType;
    private PartyInfo driver;
    private PartyInfo rider;
    private String scheduledFor;
    private String startedAt;
    private String finishedAt;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public PartyInfo getDriver() {
        return driver;
    }

    public PartyInfo getRider() {
        return rider;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    /** "2026-06-01T14:01:09.3 -> "2026-06-01 14:01" */
    public String getDisplayDate() {
        if (finishedAt == null) return "";
        String s = finishedAt.replace('T', ' ');
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
    public static class PartyInfo {
        private Long id;
        private String firstName;
        private String lastName;

        public String fullName() {
            String f = firstName != null ? firstName : "";
            String l = lastName != null ? lastName : "";
            return (f + " " + l).trim();
        }
    }


}

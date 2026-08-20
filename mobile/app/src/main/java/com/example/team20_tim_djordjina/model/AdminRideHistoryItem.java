package com.example.team20_tim_djordjina.model;

import java.util.List;

public class AdminRideHistoryItem {

    private RideHistoryItem ride;
    private boolean cancelled;
    private String cancelledBy;
    private String cancelReason;
    private boolean panicTriggered;
    private Integer driverRating;
    private Integer vehicleRating;
    private List<String> inconsistencyReports;

    public RideHistoryItem getRide() {
        return ride;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public boolean isPanicTriggered() {
        return panicTriggered;
    }

    public Integer getDriverRating() {
        return driverRating;
    }

    public Integer getVehicleRating() {
        return vehicleRating;
    }

    public List<String> getInconsistencyReports() {
        return inconsistencyReports;
    }
}

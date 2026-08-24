package com.example.team20_tim_djordjina.model;

public class DriverState {
    private boolean loggedIn;
    private boolean active;
    private boolean available;
    private boolean hasActiveRide;
    private Integer workingMinutesLast24Hours;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isHasActiveRide() {
        return hasActiveRide;
    }

    public Integer getWorkingMinutesLast24Hours() {
        return workingMinutesLast24Hours;
    }
}

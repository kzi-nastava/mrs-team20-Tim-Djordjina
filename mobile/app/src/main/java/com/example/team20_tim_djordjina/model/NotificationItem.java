package com.example.team20_tim_djordjina.model;

public class NotificationItem {

    private Long id;
    private String type;
    private String message;
    private Long relatedRideId;
    private boolean read;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Long getRelatedRideId() {
        return relatedRideId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    /** "2026-01-01T12:00:00.1" -> "2026-01-01 12:00" */
    public String getDisplayTime() {
        if (createdAt == null) return "";
        String s = createdAt.replace('T', ' ');
        return s.length() >= 16 ? s.substring(0, 16) : s;
    }
}

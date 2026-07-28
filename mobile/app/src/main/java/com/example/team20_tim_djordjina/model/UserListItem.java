package com.example.team20_tim_djordjina.model;

import com.google.gson.annotations.SerializedName;

/* One row in the admin user/driver list. Mirrors the backend UserListItemDTO */
public class UserListItem {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    @SerializedName(value = "activated", alternate = {"isActivated"})
    private boolean activated;

    @SerializedName(value = "blocked", alternate = {"isBlocked"})
    private boolean blocked;

    private String blockNote;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public String getBlockNote() {
        return blockNote;
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}

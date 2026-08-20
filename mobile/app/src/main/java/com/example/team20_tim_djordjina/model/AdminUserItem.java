package com.example.team20_tim_djordjina.model;

/**
 * Minimal user for the admin picker spinner
 * */
public class AdminUserItem {
    private Long id;
    private String firstName;
    private String lastName;
    private String role;

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String label() {
        String f = firstName != null ? firstName : "";
        String l = lastName != null ? lastName : "";
        return (f + " " + l).trim() + (role != null ? " (" + role + ")" : "");
    }
}

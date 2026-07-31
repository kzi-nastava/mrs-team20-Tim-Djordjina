package com.example.team20_tim_djordjina.model;

/** One pending driver change request, for the admin review screen. */
public class ProfileChangeRequestItem {

    private Long id;
    private Long userId;
    private String driverEmail;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private String status;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getStatus() {
        return status;
    }

    public String getFullName(){
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }
}

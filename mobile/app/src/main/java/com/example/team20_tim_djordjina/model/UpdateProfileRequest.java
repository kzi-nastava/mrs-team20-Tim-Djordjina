package com.example.team20_tim_djordjina.model;

/** Body for PUT /api/users/me */
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profilePicture;

    public UpdateProfileRequest(String firstName, String lastName,
                                String phoneNumber, String address, String profilePicture) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profilePicture = profilePicture;
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
}

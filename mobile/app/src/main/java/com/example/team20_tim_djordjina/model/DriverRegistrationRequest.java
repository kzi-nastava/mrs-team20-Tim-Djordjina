package com.example.team20_tim_djordjina.model;

/* Body for POST /api/admin/drivers (ADMIN only)
   Mirrors the backend DriverRegistrationDTO
 */
public class DriverRegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private VehicleRequest vehicle;

    public DriverRegistrationRequest(String firstName, String lastName, String email, String phoneNumber, String address, VehicleRequest vehicle) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.vehicle = vehicle;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public VehicleRequest getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleRequest vehicle) {
        this.vehicle = vehicle;
    }
}

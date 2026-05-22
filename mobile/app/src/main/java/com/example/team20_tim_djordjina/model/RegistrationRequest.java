package com.example.team20_tim_djordjina.model;

public class RegistrationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phoneNumber;
    private String address;

    public RegistrationRequest(String firstName, String lastName, String email,
                               String password, String confirmPassword,
                               String phoneNumber, String address){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Getters and Setters
    public String getFirstName(){ return this.firstName; }
    public void setFirstName(String firstName){ this.firstName = firstName; }
    public String getLastName(){ return this.lastName; }
    public void setLastName(String lastName){ this.lastName = lastName; }
    public String getEmail(){ return this.email; }
    public void setEmail(String email){ this.email = email; }
    public String getPassword(){ return this.password; }
    public void setPassword(String password){ this.password = password; }
    public String getConfirmPassword(){ return this.confirmPassword; }
    public void setConfirmPassword(String confirmPassword){ this.confirmPassword = confirmPassword; }
    public String getPhoneNumber(){ return this.phoneNumber; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
    public String getAddress(){ return this.address; }
    public void setAddress(String address){ this.address = address; }
}

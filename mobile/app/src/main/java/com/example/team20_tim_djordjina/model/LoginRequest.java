package com.example.team20_tim_djordjina.model;

/**
 * Request body sent to POST /api/auth/login
 */
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password){
        this.email = email;
        this.password = password;
    }

    public String getEmail(){ return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword(){ return password; }
    public void setPassword(String password) { this.password = password; }

}

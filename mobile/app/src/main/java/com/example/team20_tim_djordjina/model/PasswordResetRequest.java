package com.example.team20_tim_djordjina.model;

/* Body for POST /api/auth/forgot-password */
public class PasswordResetRequest {
    private String email;

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

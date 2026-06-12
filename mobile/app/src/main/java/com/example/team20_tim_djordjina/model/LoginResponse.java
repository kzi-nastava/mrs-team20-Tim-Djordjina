package com.example.team20_tim_djordjina.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response from POST /api/auth/login
 * Matches backend JSON:
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "data": {
 *     "token": "...",
 *     "tokenType": "Bearer",
 *     "userId": 1,
 *     "email": "john@example.com",
 *     "firstName": "John",
 *     "lastName": "Doe",
 *     "role": "USER",
 *     "isDriver": false,
 *     "workingHours": null
 *   }
 * }
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private LoginData data;

    // Getter and Setters
    public boolean isSuccess(){ return success; }
    public void setSuccess(boolean success){ this.success = success; }
    public String getMessage(){ return message; }
    public void setMessage(String message){ this.message = message; }
    public LoginData getData(){ return data; }
    public void setData(LoginData data){ this.data = data; }

    public static class LoginData {
        private String token;
        private String tokenType;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        @SerializedName(value = "isDriver", alternate = {"driver"})
        private boolean isDriver;
        private Integer workingHours;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isDriver() {
            return isDriver;
        }

        public void setDriver(boolean driver) {
            isDriver = driver;
        }

        public Integer getWorkingHours() {
            return workingHours;
        }

        public void setWorkingHours(Integer workingHours) {
            this.workingHours = workingHours;
        }
    }
}

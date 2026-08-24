package com.example.team20_tim_djordjina.model;

/** Response from GET /api/users/me. Mirrors the backend ProfileResponseDTO */
public class ProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String profilePicture;
    private String role;

    // Driver-only (null for others)
    private Integer workingMinutesLast24Hours;
    private Boolean hasPendingProfileChanges;
    private VehicleInfo vehicle;

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

    public String getAddress() {
        return address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getRole() {
        return role;
    }

    public Integer getWorkingMinutesLast24Hours() {
        return workingMinutesLast24Hours;
    }

    public Boolean getHasPendingProfileChanges() {
        return hasPendingProfileChanges;
    }

    public VehicleInfo getVehicle() {
        return vehicle;
    }

    public boolean isDriver() {
        return "DRIVER".equals(role);
    }

    public static class VehicleInfo {
        private String model;
        private String vehicleType;
        private String licensePlate;
        private int seats;
        private boolean babyTransport;
        private boolean petTransport;

        public String getModel() {
            return model;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public int getSeats() {
            return seats;
        }

        public boolean isBabyTransport() {
            return babyTransport;
        }

        public boolean isPetTransport() {
            return petTransport;
        }
    }
}

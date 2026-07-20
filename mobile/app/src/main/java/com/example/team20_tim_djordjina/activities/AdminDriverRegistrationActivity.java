package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminDriverRegistrationBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.DriverRegistrationRequest;
import com.example.team20_tim_djordjina.model.VehicleRequest;
import com.example.team20_tim_djordjina.util.TokenManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDriverRegistrationActivity extends AppCompatActivity {

    private ActivityAdminDriverRegistrationBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_driver_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityAdminDriverRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();
        tokenManager = new TokenManager(this);

        // Client-side guards: only admins should reach this screen
        if (!"ADMIN".equals(tokenManager.getRole())) {
            new AlertDialog.Builder(this)
                    .setTitle("Not allowed")
                    .setMessage("Only administrators can register drivers")
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> finish())
                    .show();
            return;
        }

        setupVehicleTypeSpinner();
        binding.btnRegisterDriver.setOnClickListener(v -> attemptRegisterDriver());

    }

    private void setupVehicleTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.vehicle_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVehicleType.setAdapter(adapter);
        binding.spinnerVehicleType.setSelection(0);
    }

    private void attemptRegisterDriver() {
        hideError();

        String firstName = text(binding.etDriverFirstName);
        String lastName = text(binding.etDriverLastName);
        String email = text(binding.etDriverEmail);
        String phoneNumber = text(binding.etDriverPhone);
        String address = text(binding.etDriverAddress);

        String model = text(binding.etVehicleModel);
        String vehicleType = binding.spinnerVehicleType.getSelectedItem().toString();
        String licensePlate = text(binding.etLicensePlate);
        String seatsText = text(binding.etSeats);
        boolean babyTransport = binding.switchBabyTransport.isChecked();
        boolean petTransport = binding.switchPetTransport.isChecked();

        if (!validate(firstName,lastName,email,phoneNumber,address,model,licensePlate, seatsText)){
            return;
        }

        int seats = Integer.parseInt(seatsText);

        VehicleRequest vehicle = new VehicleRequest(
                model, vehicleType, licensePlate, seats, babyTransport, petTransport);
        DriverRegistrationRequest request = new DriverRegistrationRequest(
                firstName, lastName, email, phoneNumber, address, vehicle);

        setLoading(true);
        apiService.registerDriver(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    showSuccess(email);
                } else {
                    showError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private boolean validate(String firstName, String lastName, String email,
                             String phoneNumber, String address,
                             String model, String licensePlate, String seatsText){
        if (TextUtils.isEmpty(firstName)) {
            showError("Please enter the driver's first name");
            return false;
        }
        if (firstName.length() < 2 || firstName.length() > 50){
            showError("First name must be between 2 and 50 characters");
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            showError("Please enter the driver's last name");
            return false;
        }
        if (lastName.length() < 2 || lastName.length() > 50){
            showError("Last name must be between 2 and 50 characters");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            showError("Please enter the driver's email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showError("Please enter a valid email");
            return false;
        }
        if (email.length() > 100){
            showError("Email must not exceed 100 characters");
            return false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Please enter the driver's phone number");
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            showError("Please enter the driver's address");
            return false;
        }
        if (address.length() > 255){
            showError("Address must not exceed 255 characters");
            return false;
        }

        if (TextUtils.isEmpty(model)) {
            showError("Please enter the vehicle model");
            return false;
        }
        if (TextUtils.isEmpty(licensePlate)) {
            showError("Please enter the license plate");
            return false;
        }

        if (TextUtils.isEmpty(seatsText)) {
            showError("Please enter the number of seats");
            return false;
        }
        int seats;
        try {
            seats = Integer.parseInt(seatsText);
        } catch (NumberFormatException e){
            showError("Number of seats must be a number");
            return false;
        }
        if (seats < 1) {
            showError("Vehicle must have at least 1 seat");
            return false;
        }

        return true;
    }

    private String parseError(Response<ApiResponse> response){
        try {
            if (response.errorBody() != null) {
                ApiResponse err = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (err != null && err.getMessage() != null && !err.getMessage().isEmpty()){
                    return err.getMessage();
                }
            }
        } catch (Exception ignored) {
            // fall through to status-based message
        }
        switch (response.code()){
            case 409: return "A user with this email already exists.";
            case 400: return "Invalid data. Please check the fields and try again.";
            case 403: return "Only administrators can register drivers.";
            default: return "Could not register the driver. Please try again.";
        }
    }

    private void showSuccess(String email){
        new AlertDialog.Builder(this)
                .setTitle("Driver registered")
                .setMessage("An email has been sent to " + email
                        + " with a link to set their password. The link is valid for 24 hours.")
                .setCancelable(false)
                .setPositiveButton("Done", (d, w) -> finish())
                .setNegativeButton("Register another", (d, w) -> clearForm())
                .show();
    }

    private void clearForm(){
        binding.etDriverFirstName.setText("");
        binding.etDriverLastName.setText("");
        binding.etDriverEmail.setText("");
        binding.etDriverPhone.setText("");
        binding.etDriverAddress.setText("");
        binding.etVehicleModel.setText("");
        binding.etLicensePlate.setText("");
        binding.etSeats.setText("");
        binding.switchBabyTransport.setChecked(false);
        binding.switchPetTransport.setChecked(false);
        binding.spinnerVehicleType.setSelection(0);
        hideError();
    }
    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading){
        binding.btnRegisterDriver.setEnabled(!loading);
        binding.btnRegisterDriver.setText(loading ?
                getString(R.string.registering) : getString(R.string.register_driver));
    }

    private void showError(String message) {
        binding.tvDriverRegError.setText(message);
        binding.tvDriverRegError.setVisibility(View.VISIBLE);
    }
    private void hideError() {
        binding.tvDriverRegError.setText("");
        binding.tvDriverRegError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
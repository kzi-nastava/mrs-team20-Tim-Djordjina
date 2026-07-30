package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityProfileBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.ProfileResponse;
import com.example.team20_tim_djordjina.model.UpdateProfileRequest;
import com.example.team20_tim_djordjina.util.TokenManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;

    private String currentProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();
        tokenManager = new TokenManager(this);

        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        //binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog);
        binding.btnLogout.setOnClickListener(v -> logout());

        loadProfile();

    }

    private void loadProfile() {
        setLoading(true);
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindProfile(response.body());
                } else {
                    showError(getString(R.string.could_not_load_profile));
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error));
            }
        });
    }

    private void bindProfile(ProfileResponse p) {
        hideError();
        currentProfilePicture = p.getProfilePicture();

        binding.etFirstName.setText(p.getFirstName());
        binding.etLastName.setText(p.getLastName());
        binding.tvEmailValue.setText(p.getEmail());
        binding.etPhone.setText(p.getPhoneNumber());
        binding.etAddress.setText(p.getAddress());

        if (p.isDriver()) {
            binding.driverSection.setVisibility(View.VISIBLE);

            int minutes = p.getWorkingMinutesLast24Hours() == null ? 0 : p.getWorkingMinutesLast24Hours();
            binding.tvWorkingHours.setText(getString(R.string.working_hours_value, minutes / 60, minutes % 60));

            ProfileResponse.VehicleInfo v = p.getVehicle();
            if (v != null) {
                binding.tvVehicleInfo.setText(getString(
                        R.string.vehicle_info_value,
                        v.getModel(), v.getVehicleType(), v.getLicensePlate(), v.getSeats()));
            } else {
                binding.tvVehicleInfo.setText(R.string.no_vehicle);
            }

            boolean pending = Boolean.TRUE.equals(p.getHasPendingProfileChanges());
            binding.tvPendingApproval.setVisibility(pending ? View.VISIBLE : View.GONE);
        } else {
            binding.driverSection.setVisibility(View.GONE);
            binding.tvPendingApproval.setVisibility(View.GONE);
        }


    }

    private void saveProfile() {
        hideError();
        String firstName = text(binding.etFirstName.getText());
        String lastName = text(binding.etLastName.getText());
        String phone = text(binding.etPhone.getText());
        String address = text(binding.etAddress.getText());

        if (TextUtils.isEmpty(firstName) || firstName.length() < 2 || firstName.length() > 50) {
            showError(getString(R.string.first_name_length));
            return;
        }
        if (TextUtils.isEmpty(lastName) || lastName.length() < 2 || lastName.length() > 50) {
            showError(getString(R.string.last_name_length));
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            showError(getString(R.string.phone_required));
            return;
        }
        if (TextUtils.isEmpty(address) || address.length() > 255) {
            showError(getString(R.string.address_invalid));
            return;
        }

        UpdateProfileRequest req = new UpdateProfileRequest(
                firstName, lastName, phone, address, currentProfilePicture
        );

        setLoading(true);
        apiService.updateProfile(req).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : getString(R.string.profile_saved);
                    Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
                    loadProfile();      // refresh -> shows pending banner for drviers
                } else {
                    showError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error));
            }
        });

    }

    private void showChangePasswordDialog() {
        // TODO add functionality
    }

    private void logout() {
        tokenManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String parseError(Response<ApiResponse> response) {
        try {
            if(response.errorBody() != null) {
                ApiResponse err = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (err != null && err.getMessage() != null && !err.getMessage().isEmpty()) {
                    return err.getMessage();
                }
            }
        } catch (Exception ignored) {}
        return getString(R.string.something_went_wrong);
    }

    private String text(CharSequence cs) {
        return cs == null ? "" : cs.toString().trim();
    }
    private void setLoading(boolean loading) {
        binding.progressProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
    }

    private void showError(String message) {
        binding.tvProfileError.setText(message);
        binding.tvProfileError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvProfileError.setText("");
        binding.tvProfileError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }
}
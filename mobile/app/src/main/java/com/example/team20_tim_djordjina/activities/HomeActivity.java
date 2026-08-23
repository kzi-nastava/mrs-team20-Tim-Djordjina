package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityHomeBinding;
import com.example.team20_tim_djordjina.model.RideResponse;
import com.example.team20_tim_djordjina.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ApiService apiService;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        tokenManager = new TokenManager(this);

        setupVisibility();
        setupClickListeners();

    }

    private void setupVisibility() {
        String role = tokenManager.getRole();
        boolean loggedIn = role != null && !role.isEmpty();
        boolean isUser = "USER".equals(role);
        boolean isDriver = "DRIVER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);

        // Landing (logged-out) controls
        show(binding.btnHomeLogin, !loggedIn);
        show(binding.btnHomeSignup, !loggedIn);

        // Everyone who is logged in
        show(binding.btnProfile, loggedIn);
        show(binding.btnNotifications, loggedIn);
        show(binding.btnSharedWithMe, loggedIn);
        show(binding.btnRideHistory, loggedIn);

        // Passenger only
        show(binding.btnRequestRide, isUser);
        show(binding.btnFavourites, isUser);
        show(binding.btnRiderCurrentRide, isUser);

        // Driver only
        show(binding.btnCurrentRide, isDriver);
        show(binding.btnDriverStatus, isDriver);

        // Admin only
        show(binding.btnAdminRegisterDriver, isAdmin);
        show(binding.btnAdminManageUsers, isAdmin);
        show(binding.btnAdminProfileChanges, isAdmin);
        show(binding.btnAdminPricing, isAdmin);
        show(binding.btnAdminRides, isAdmin);
        show(binding.btnAdminPanics, isAdmin);
        show(binding.btnAdminUserHistory, isAdmin);

    }

    private void setupClickListeners() {
        // Landing (logged-out) controls
        binding.btnHomeLogin.setOnClickListener(v -> open(LoginActivity.class));
        binding.btnHomeSignup.setOnClickListener(v -> open(RegisterActivity.class));

        // Everyone who is logged in
        binding.btnProfile.setOnClickListener(v -> open(ProfileActivity.class));
        binding.btnNotifications.setOnClickListener(v -> open(NotificationsActivity.class));
        binding.btnSharedWithMe.setOnClickListener(v -> open(LinkedRidesActivity.class));
        binding.btnRideHistory.setOnClickListener(v -> open(RideHistoryActivity.class));

        // Passenger only
        binding.btnRequestRide.setOnClickListener(v -> open(RideRequestActivity.class));
        binding.btnFavourites.setOnClickListener(v -> open(FavouritesActivity.class));
        binding.btnRiderCurrentRide.setOnClickListener(v -> openRiderCurrentRide());

        // Driver only
        binding.btnCurrentRide.setOnClickListener(v -> open(DriverRideActivity.class));
        binding.btnDriverStatus.setOnClickListener(v -> open(DriverStatusActivity.class));

        // Admin only
        binding.btnAdminRegisterDriver.setOnClickListener(v -> open(AdminDriverRegistrationActivity.class));
        binding.btnAdminManageUsers.setOnClickListener(v -> open(AdminUserListActivity.class));
        binding.btnAdminProfileChanges.setOnClickListener(v -> open(AdminProfileChangesActivity.class));
        binding.btnAdminPricing.setOnClickListener(v -> open(AdminPricingActivity.class));
        binding.btnAdminRides.setOnClickListener(v -> open(AdminRideActivity.class));
        binding.btnAdminPanics.setOnClickListener(v -> open(AdminPanicActivity.class));
        binding.btnAdminUserHistory.setOnClickListener(v -> open(AdminUserHistoryActivity.class));

    }

    private void openRiderCurrentRide() {
        apiService.getRiderCurrentRide().enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(HomeActivity.this, RiderRideActivity.class);
                    intent.putExtra(RiderRideActivity.EXTRA_RIDE_ID, response.body().getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, getString(R.string.no_active_ride),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, getString(R.string.network_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    private void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
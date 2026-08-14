package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.databinding.ActivityHomeBinding;
import com.example.team20_tim_djordjina.util.TokenManager;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

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

        // Driver only
        show(binding.btnCurrentRide, isDriver);
        show(binding.btnDriverStatus, isDriver);

        // Admin only
        show(binding.btnAdminRegisterDriver, isAdmin);
        show(binding.btnAdminManageUsers, isAdmin);
        show(binding.btnAdminProfileChanges, isAdmin);
        show(binding.btnAdminPricing, isAdmin);

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

        // Driver only
        binding.btnCurrentRide.setOnClickListener(v -> open(DriverRideActivity.class));
        binding.btnDriverStatus.setOnClickListener(v -> open(DriverStatusActivity.class));

        // Admin only
        binding.btnAdminRegisterDriver.setOnClickListener(v -> open(AdminDriverRegistrationActivity.class));
        binding.btnAdminManageUsers.setOnClickListener(v -> open(AdminUserListActivity.class));
        binding.btnAdminProfileChanges.setOnClickListener(v -> open(AdminProfileChangesActivity.class));
        binding.btnAdminPricing.setOnClickListener(v -> open(AdminPricingActivity.class));

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
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = new TokenManager(this);

        if ("ADMIN".equals(tokenManager.getRole())){
            binding.btnAdminRegisterDriver.setVisibility(View.VISIBLE);
            binding.btnAdminRegisterDriver.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AdminDriverRegistrationActivity.class)));
            binding.btnAdminManageUsers.setVisibility(View.VISIBLE);
            binding.btnAdminManageUsers.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AdminUserListActivity.class)));
            binding.btnAdminProfileChanges.setVisibility(View.VISIBLE);
            binding.btnAdminProfileChanges.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, AdminProfileChangesActivity.class)));

        }
        if ("DRIVER".equals(tokenManager.getRole())) {
            binding.btnRequestRide.setVisibility(View.GONE);

            binding.btnCurrentRide.setVisibility(View.VISIBLE);
            binding.btnCurrentRide.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, DriverRideActivity.class)));

        }

        if ("USER".equals(tokenManager.getRole())) {
            binding.btnFavourites.setVisibility(View.VISIBLE);
            binding.btnFavourites.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, FavouritesActivity.class)));
        } else {
            binding.btnFavourites.setVisibility(View.GONE);
        }

        binding.btnHomeLogin.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        binding.btnHomeSignup.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RegisterActivity.class)));
        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
        binding.btnRequestRide.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RideRequestActivity.class)));
        binding.btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationsActivity.class)));
        binding.btnSharedWithMe.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LinkedRidesActivity.class)));
    }
}
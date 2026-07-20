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
        }

        binding.btnHomeLogin.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));
        binding.btnHomeSignup.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RegisterActivity.class)));
    }
}
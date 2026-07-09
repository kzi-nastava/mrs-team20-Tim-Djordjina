package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityResetPasswordBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.PasswordResetSubmit;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reset_password_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        // The deep-link is rideon://reset-password?token=<token>
        token = extractToken();
        if (TextUtils.isEmpty(token)){
            binding.btnResetPassword.setEnabled(false);
            showError("This reset link is invalid or incomplete. Please request a new one.");
        }

        binding.btnResetPassword.setOnClickListener(v -> attemptReset());
    }

    // Reads the ?token=... query parameter from the incoming deep-link Intent
    private String extractToken() {
        Uri data = getIntent() != null ? getIntent().getData() : null;
        if (data != null) {
            return data.getQueryParameter("token");
        }
        return null;
    }

    private void attemptReset() {
        hideError();

        String newPassword = binding.etResetPassword.getText() != null
                ? binding.etResetPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etResetConfirmPassword.getText() != null
                ? binding.etResetConfirmPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newPassword)){
            showError("Please enter a new password");
            return;
        }
        if (newPassword.length() < 8){
            showError("Password must be at least 8 characters");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            showError("Please confirm your password");
            return;
        }
        if (!newPassword.equals(confirmPassword)){
            showError("Passwords do not match");
            return;
        }

        setLoading(true);
        PasswordResetSubmit body = new PasswordResetSubmit(token, newPassword, confirmPassword);
        apiService.resetPassword(body).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    showSuccess();
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

    private String parseError(Response<ApiResponse> response){
        try {
            if (response.errorBody() != null) {
                ApiResponse err = new Gson().fromJson(response.errorBody().string(), ApiResponse.class);
                if (err != null && err.getMessage() != null && !err.getMessage().isEmpty()) {
                    return err.getMessage();
                }
            }
        } catch (Exception ignored) {

        }
        return "Could not reset password. The link may have expired";
    }

    private void showSuccess(){
        new AlertDialog.Builder(this)
                .setTitle("Password updated")
                .setMessage("Your password has been reset. Please log in with your new password.")
                .setCancelable(false)
                .setPositiveButton("Go to login", (d, w) -> {
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void setLoading(boolean loading) {
        binding.btnResetPassword.setEnabled(!loading);
        binding.btnResetPassword.setText(loading ? "Resetting...": getString(R.string.reset_password_button));
    }
    private void hideError() {
        binding.tvResetError.setText("");
        binding.tvResetError.setVisibility(View.GONE);
    }
    private void showError(String message) {
        binding.tvResetError.setText(message);
        binding.tvResetError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }

}
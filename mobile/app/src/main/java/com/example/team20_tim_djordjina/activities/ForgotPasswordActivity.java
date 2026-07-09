package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityForgotPasswordBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.PasswordResetRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        binding.btnSendReset.setOnClickListener(v -> attemptSendReset());
        binding.tvBackToLogin.setOnClickListener(v -> goToLogin());
    }

    private void attemptSendReset() {
        hideError();

        String email = binding.etForgotEmail.getText() != null
                ? binding.etForgotEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            showError("Please enter your email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showError("Please enter a valid email");
            return;
        }

        setLoading(true);
        apiService.forgotPassword(new PasswordResetRequest(email)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    // Backend returns a generic 200 whether or not email exists
                    showConfirmation();
                } else {
                    showError("Something went wrong. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void showConfirmation(){
        new AlertDialog.Builder(this)
                .setTitle("Check your email")
                .setMessage("If an account exists for that email, we've sent a password reset link. "
                        + "Open it on this phone to set a new password. The link expires in 1 hour.")
                .setCancelable(false)
                .setPositiveButton("Back to login", (d, w) -> goToLogin())
                .show();
    }
    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        binding.btnSendReset.setEnabled(!loading);
        binding.btnSendReset.setText(loading ? "Sending..." : getString(R.string.send_reset_link));
    }

    private void showError(String message) {
        binding.tvForgotError.setText(message);
        binding.tvForgotError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvForgotError.setText("");
        binding.tvForgotError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }
}
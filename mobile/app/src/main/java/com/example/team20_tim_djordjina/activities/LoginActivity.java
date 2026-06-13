package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityLoginBinding;
import com.example.team20_tim_djordjina.model.LoginRequest;
import com.example.team20_tim_djordjina.model.LoginResponse;
import com.example.team20_tim_djordjina.util.TokenManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvError,tvSignupRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // View binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dependencies
        apiService = RetrofitClient.getInstance(this).getApiService();
        tokenManager = new TokenManager(this);

        // Click listeners
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });

        binding.tvSignupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });


    }

    private void attemptLogin(){
        hideError();

        String email = binding.etLoginEmail.getText() != null
                ? binding.etLoginEmail.getText().toString().trim() : "";
        String password = binding.etLoginPassword.getText() != null
                ? binding.etLoginPassword.getText().toString().trim() : "";

        // Client side validation
        if (TextUtils.isEmpty(email)){
            showError("Please enter your email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showError("Please enter a valid email address");
            return;
        }
        if (TextUtils.isEmpty(password)){
            showError("Please enter your password");
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful()
                    && response.body() != null
                    && response.body().isSuccess()
                    && response.body().getData() != null){

                    LoginResponse.LoginData data = response.body().getData();

                    // Save JWT plus user info
                    tokenManager.saveSession(
                            data.getToken(),
                            data.getUserId(),
                            data.getEmail(),
                            data.getRole()
                    );

                    Toast.makeText(LoginActivity.this,
                            "Welcome, " + data.getFirstName() + "!",
                            Toast.LENGTH_SHORT).show();

                    navigateAfterLogin(data.getRole());
                } else {
                    // Error response
                    showError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private String parseErrorMessage(Response<LoginResponse> response){
        try {
            if (response.errorBody() != null){
                String json = response.errorBody().string();
                LoginResponse error = new Gson().fromJson(json, LoginResponse.class);
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()){
                    return error.getMessage();
                }
            }
        } catch (Exception ignored){

        }
        return "Login failed. Please try again.";

    }

    /**
     * Send the user to their home screen after a successful login.
     * Clears the back stack so pressing "back" won't return to the login screen
     */
    private void navigateAfterLogin(String role){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("USER", role);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // UI helpers
    private void setLoading(boolean loading){
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? "Logging in..." : getString(R.string.login));
    }
    private void showError(String message){
        binding.tvLoginError.setText(message);
        binding.tvLoginError.setVisibility(View.VISIBLE);
    }
    private void hideError(){
        binding.tvLoginError.setText("");
        binding.tvLoginError.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }

}
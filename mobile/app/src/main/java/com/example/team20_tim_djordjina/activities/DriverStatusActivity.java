package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityDriverStatusBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.DriverActiveRequest;
import com.example.team20_tim_djordjina.model.DriverState;
import com.example.team20_tim_djordjina.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverStatusActivity extends AppCompatActivity {

    private ActivityDriverStatusBinding binding;
    private ApiService apiService;
    private TokenManager tokenManager;

    private boolean updatingUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_status);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityDriverStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();
        tokenManager = new TokenManager(this);

        binding.switchDuty.setOnCheckedChangeListener((btn, checked) -> {
            if (updatingUi) return;
            setActive(checked);
        });
        binding.btnDriverLogout.setOnClickListener(v -> logout());

        loadState();
    }

    private void loadState() {
        setLoading(true);
        apiService.getDriverState().enqueue(new Callback<DriverState>() {
            @Override
            public void onResponse(Call<DriverState> call, Response<DriverState> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    render(response.body());
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<DriverState> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void setActive(boolean active) {
        apiService.setDriverActive(new DriverActiveRequest(active))
                .enqueue(new Callback<DriverState>() {
                    @Override
                    public void onResponse(Call<DriverState> call, Response<DriverState> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            render(response.body());
                        } else {
                            revertSwitch(!active);
                            toast(getString(R.string.something_went_wrong));
                        }
                    }

                    @Override
                    public void onFailure(Call<DriverState> call, Throwable t) {
                        revertSwitch(!active);
                        toast(getString(R.string.network_error));
                    }
                });
    }

    private void logout() {
        apiService.driverLogout().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearSession();
                    goToLogin();
                } else if(response.code() == 409) {
                    toast(getString(R.string.logout_blocked_active_ride));
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void render(DriverState s) {
        revertSwitch(s.isActive());

        binding.tvDutyLabel.setText(s.isActive()
                ? getString(R.string.on_duty) : getString(R.string.off_duty));

        int minutes = s.getWorkingMinutesLast24Hours() != null
                ? s.getWorkingMinutesLast24Hours() : 0;
        binding.tvStatusInfo.setText(getString(R.string.driver_status_info,
                s.isAvailable() ? getString(R.string.yes) : getString(R.string.no), minutes));
    }

    private void revertSwitch(boolean value) {
        updatingUi = true;
        binding.switchDuty.setChecked(value);
        updatingUi = false;
    }

    private void setLoading(boolean loading) {
        binding.progressStatus.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
package com.example.team20_tim_djordjina.activities;

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
import com.example.team20_tim_djordjina.databinding.ActivityDriverRideBinding;
import com.example.team20_tim_djordjina.model.RideResponse;
import com.example.team20_tim_djordjina.model.RideStopRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideActivity extends AppCompatActivity {

    private ActivityDriverRideBinding binding;
    private ApiService apiService;
    private Long currentRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_ride);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityDriverRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        binding.btnStartRide.setOnClickListener(v -> startRide());
        binding.btnFinishRide.setOnClickListener(v -> finishRide());
        binding.btnRefreshRide.setOnClickListener(v -> loadCurrentRide());

        loadCurrentRide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentRide();
    }

    private void loadCurrentRide() {
        setLoading(true);
        apiService.getCurrentRide().enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    // 204 No Content -> body is null -> no active ride
                    if (response.body() == null) {
                        showNoRide();
                    } else {
                        bindRide(response.body());
                    }
                } else {
                    toast(getString(R.string.could_not_load_ride));
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {

            }
        });
    }

    private void showNoRide() {
        currentRideId = null;
        binding.rideDetails.setVisibility(View.GONE);
        binding.btnStartRide.setVisibility(View.GONE);
        binding.btnFinishRide.setVisibility(View.GONE);
        binding.tvNoActiveRide.setVisibility(View.VISIBLE);
    }

    private void bindRide(RideResponse r) {
        currentRideId = r.getId();

        binding.tvNoActiveRide.setVisibility(View.GONE);
        binding.rideDetails.setVisibility(View.VISIBLE);

        binding.tvPickupValue.setText(r.getPickupAddress());
        binding.tvDestinationValue.setText(r.getDestinationAddress());
        binding.tvVehicleTypeValue.setText(r.getVehicleType());
        binding.tvFareValue.setText(getString(R.string.fare_label, r.getFare()));
        binding.tvDistanceValue.setText(getString(R.string.distance_label, r.getDistanceKm()));

        // Stops (ordered)
        if (r.getStops() != null && !r.getStops().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RideStopRequest s : r.getStops()) {
                sb.append("• ").append(s.getAddress()).append("\n");
            }
            binding.tvStopsValue.setText(sb.toString().trim());
            binding.stopsSection.setVisibility(View.VISIBLE);
        } else {
            binding.stopsSection.setVisibility(View.GONE);
        }

        // Status
        String status = r.getStatus();
        if ("ASSIGNED".equals(status)) {
            binding.tvRideStatus.setText(R.string.status_assigned);
            binding.btnStartRide.setText(View.VISIBLE);
            binding.btnFinishRide.setText(View.GONE);
        } else if ("IN_PROGRESS".equals(status)) {
            binding.tvRideStatus.setText(R.string.status_in_progress);
            binding.btnStartRide.setText(View.GONE);
            binding.btnFinishRide.setText(View.VISIBLE);
        } else {
            binding.tvRideStatus.setText(status);
            binding.btnStartRide.setText(View.GONE);
            binding.btnFinishRide.setText(View.GONE);
        }
    }

    private void startRide() {
        if (currentRideId == null) return;
        setLoading(true);
        apiService.startRide(currentRideId).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    toast(getString(R.string.ride_started));
                    bindRide(response.body());
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void finishRide() {
        if (currentRideId == null) return;
        setLoading(true);
        apiService.finishRide(currentRideId).enqueue(new Callback<RideResponse>() {
            @Override
            public void onResponse(Call<RideResponse> call, Response<RideResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    toast(getString(R.string.ride_finished));
                    loadCurrentRide();
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<RideResponse> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressDriverRide.setVisibility(loading ? View.VISIBLE : View.GONE);
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
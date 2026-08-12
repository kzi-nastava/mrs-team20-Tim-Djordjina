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
import com.example.team20_tim_djordjina.databinding.ActivityRideHistoryDetailBinding;
import com.example.team20_tim_djordjina.model.RideHistoryItem;
import com.example.team20_tim_djordjina.model.RideStopRequest;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideHistoryDetailActivity extends AppCompatActivity {

    private ActivityRideHistoryDetailBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_history_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityRideHistoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long rideId = getIntent().getLongExtra(RideHistoryActivity.EXTRA_RIDE_ID, -1);
        if (rideId <= 0) {
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance(this).getApiService();
        apiService.getRideHistoryDetail(rideId).enqueue(new Callback<RideHistoryItem>() {
            @Override
            public void onResponse(Call<RideHistoryItem> call, Response<RideHistoryItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bind(response.body());
                } else {
                    toast(getString(R.string.could_not_load_history));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<RideHistoryItem> call, Throwable t) {
                toast(getString(R.string.network_error));
                finish();
            }
        });

    }

    private void bind(RideHistoryItem r) {
        binding.tvDetailRoute.setText(r.getPickupAddress() + " -> " + r.getDestinationAddress());
        binding.tvDetailStatus.setText(r.getStatus());
        binding.tvDetailFare.setText(String.format(Locale.US, "%.2f RSD", r.getFare()));
        binding.tvDetailDistance.setText(
                getString(R.string.distance_label, r.getDistanceKm()));

        if (r.getStops() != null && !r.getStops().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RideStopRequest s : r.getStops()) sb.append("• ").append(s.getAddress()).append("\n");
            binding.tvDetailStops.setText(sb.toString().trim());
            binding.detailStopsSection.setVisibility(View.VISIBLE);
        } else {
            binding.detailStopsSection.setVisibility(View.GONE);
        }

        binding.tvDetailDriver.setText(getString(R.string.history_driver,
                r.getDriver() != null ? r.getDriver().fullName() : "-"));
        binding.tvDetailRider.setText(getString(R.string.history_rider,
                r.getRider() != null ? r.getRider().fullName() : "-"));

        binding.tvDetailStarted.setText(getString(R.string.history_started,
                r.getStartedAt() != null ? r.getStartedAt().replace('T', ' ') : "-"));
        binding.tvDetailFinished.setText(getString(R.string.history_finished,
                r.getFinishedAt() != null ? r.getFinishedAt().replace('T', ' ') : "-"));

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
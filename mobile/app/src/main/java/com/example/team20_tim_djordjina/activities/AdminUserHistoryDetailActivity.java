package com.example.team20_tim_djordjina.activities;

import android.os.Bundle;
import android.view.View;import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminUserHistoryDetailBinding;
import com.example.team20_tim_djordjina.model.AdminRideHistoryItem;
import com.example.team20_tim_djordjina.model.RideHistoryItem;import com.example.team20_tim_djordjina.model.RideStopRequest;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserHistoryDetailActivity extends AppCompatActivity {

    private ActivityAdminUserHistoryDetailBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_history_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminUserHistoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long rideId = getIntent().getLongExtra(AdminUserHistoryActivity.EXTRA_RIDE_ID, -1);
        if (rideId <= 0) {
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance(this).getApiService();
        apiService.getAdminRideFull(rideId).enqueue(new Callback<AdminRideHistoryItem>() {
            @Override
            public void onResponse(Call<AdminRideHistoryItem> call, Response<AdminRideHistoryItem> response) {
                if (response.isSuccessful() && response.body() != null){
                    bind(response.body());
                } else {
                    toast(getString(R.string.something_went_wrong));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<AdminRideHistoryItem> call, Throwable t) {
                toast(getString(R.string.network_error));
                finish();
            }
        });
    }

    private void bind(AdminRideHistoryItem item) {
        RideHistoryItem r = item.getRide();

        binding.tvDetailRoute.setText(r.getPickupAddress() + " -> " + r.getDestinationAddress());
        binding.tvDetailStatus.setText(r.getStatus());
        binding.tvDetailFare.setText(String.format(Locale.US, "%.2f RSD", r.getFare()));
        binding.tvDetailDistance.setText(getString(R.string.distance_label, r.getDistanceKm()));

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

        binding.tvDetailPanic.setText(getString(R.string.history_panic,
                item.isPanicTriggered() ? getString(R.string.yes) : getString(R.string.no)));

        if (item.isCancelled()) {
            String by = item.getCancelledBy() != null ? item.getCancelledBy() : "-";
            String reason = item.getCancelReason() != null ? item.getCancelReason() : "-";
            binding.tvDetailCancelled.setText(getString(R.string.history_cancelled, by, reason));
            binding.tvDetailCancelled.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailCancelled.setVisibility(View.GONE);
        }

        if (item.getDriverRating() != null) {
            binding.tvDetailRating.setText(String.format(Locale.US, "★ driver %d · vehicle %d",
                    item.getDriverRating(),
                    item.getVehicleRating() != null ? item.getVehicleRating() : 0));
            binding.tvDetailRating.setVisibility(View.VISIBLE);
        } else {
            binding.tvDetailRating.setVisibility(View.GONE);
        }

        if (item.getInconsistencyReports() != null && !item.getInconsistencyReports().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String rep : item.getInconsistencyReports()) sb.append("• ").append(rep).append("\n");
            binding.tvDetailInconsistency.setText(sb.toString().trim());
            binding.detailInconsistencySection.setVisibility(View.VISIBLE);
        } else {
            binding.detailInconsistencySection.setVisibility(View.GONE);
        }
    }


    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
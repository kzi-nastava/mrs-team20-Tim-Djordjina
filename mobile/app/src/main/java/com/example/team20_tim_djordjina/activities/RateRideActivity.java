package com.example.team20_tim_djordjina.activities;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.team20_tim_djordjina.databinding.ActivityRateRideBinding;
import com.example.team20_tim_djordjina.model.Rating;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateRideActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_ID = "ride_id";
    private ActivityRateRideBinding binding;
    private ApiService apiService;
    private long rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rate_ride);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityRateRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        rideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1);
        if (rideId <= 0) {
            finish();
            return;
        }

        binding.btnSubmitRating.setOnClickListener(v -> submit());
    }

    private void submit() {
        int driverScore = Math.round(binding.rbDriver.getRating());
        int vehicleScore = Math.round(binding.rbVehicle.getRating());
        String comment = binding.etRatingComment.getText() != null
                ? binding.etRatingComment.getText().toString().trim() : "";
        if (TextUtils.isEmpty(comment)) comment = null;

        setLoading(true);
        apiService.rateRide(rideId, new Rating(driverScore, vehicleScore, comment))
                .enqueue(new Callback<Rating>() {
                    @Override
                    public void onResponse(Call<Rating> call, Response<Rating> response) {
                        setLoading(false);
                        if (response.isSuccessful()){
                            toast(getString(R.string.rating_thanks));
                            finish();
                        } else {
                            toast(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<Rating> call, Throwable t) {
                        setLoading(false);
                        toast(getString(R.string.network_error));
                    }
                });
    }

    private String errorMessage(int code) {
        switch (code){
            case 403: return getString(R.string.rating_not_your_ride);
            case 409: return getString(R.string.rating_not_allowed);
            case 400: return getString(R.string.rating_invalid);
            default: return getString(R.string.something_went_wrong);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressRating.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSubmitRating.setEnabled(!loading);
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
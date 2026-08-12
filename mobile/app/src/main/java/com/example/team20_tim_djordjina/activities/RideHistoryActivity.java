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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.adapters.RideHistoryAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityRideHistoryBinding;
import com.example.team20_tim_djordjina.databinding.ActivityRideHistoryDetailBinding;
import com.example.team20_tim_djordjina.model.RideHistoryItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideHistoryActivity extends AppCompatActivity
        implements RideHistoryAdapter.OnRideClickListener {

    public static final String EXTRA_RIDE_ID = "ride_id";
    private ActivityRideHistoryBinding binding;
    private ApiService apiService;
    private RideHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ride_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityRideHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new RideHistoryAdapter(this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        setLoading(true);
        apiService.getRideHistory().enqueue(new Callback<List<RideHistoryItem>>() {
            @Override
            public void onResponse(Call<List<RideHistoryItem>> call, Response<List<RideHistoryItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<RideHistoryItem> rides = response.body();
                    adapter.setItems(rides);
                    binding.tvNoHistory.setVisibility(rides.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.could_not_load_history));
                }
            }

            @Override
            public void onFailure(Call<List<RideHistoryItem>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    @Override
    public void onRideClick(RideHistoryItem item) {
        Intent i = new Intent(this, ActivityRideHistoryDetailBinding.class);
        i.putExtra(EXTRA_RIDE_ID, item.getId());
        startActivity(i);
    }

    private void setLoading(boolean loading) {
        binding.progressHistory.setVisibility(loading ? View.VISIBLE : View.GONE);
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
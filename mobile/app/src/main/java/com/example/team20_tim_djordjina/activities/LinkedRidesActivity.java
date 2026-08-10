package com.example.team20_tim_djordjina.activities;

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
import com.example.team20_tim_djordjina.adapters.LinkedRideAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityLinkedRidesBinding;
import com.example.team20_tim_djordjina.model.RideResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LinkedRidesActivity extends AppCompatActivity {

    private ActivityLinkedRidesBinding binding;
    private ApiService apiService;
    private LinkedRideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_linked_rides);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityLinkedRidesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new LinkedRideAdapter();
        binding.rvLinkedRides.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLinkedRides.setAdapter(adapter);

        loadLinkedRides();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLinkedRides();
    }

    private void loadLinkedRides() {
        setLoading(true);
        apiService.getLinkedRides().enqueue(new Callback<List<RideResponse>>() {
            @Override
            public void onResponse(Call<List<RideResponse>> call, Response<List<RideResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<RideResponse> rides = response.body();
                    adapter.setItems(rides);
                    binding.tvNoLinkedRides.setVisibility(
                            rides.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.could_not_load_linked_rides));
                }
            }

            @Override
            public void onFailure(Call<List<RideResponse>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressLinkedRides.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        Toast.makeText(this, "msg", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
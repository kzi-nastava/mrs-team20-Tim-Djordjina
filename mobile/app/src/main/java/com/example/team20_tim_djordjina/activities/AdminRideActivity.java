package com.example.team20_tim_djordjina.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.adapters.AdminRideAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminRideBinding;
import com.example.team20_tim_djordjina.model.RideHistoryItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideActivity extends AppCompatActivity
    implements AdminRideAdapter.OnRideClickListener {

    public static final String EXTRA_RIDE_ID = "ride_id";
    private static final String[] STATUSES = {
            "All", "REQUESTED", "SCHEDULED", "ASSIGNED", "IN_PROGRESS", "FINISHED", "CANCELLED", "REJECTED"
    };

    private ActivityAdminRideBinding binding;
    private ApiService apiService;
    private AdminRideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_ride);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new AdminRideAdapter(this);
        binding.rvAdminRides.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdminRides.setAdapter(adapter);

        setupStatusFilter();
        loadRides(null);
    }

    private void setupStatusFilter() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, STATUSES);
        binding.spinnerStatus.setAdapter(spinnerAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                loadRides(position == 0 ? null : STATUSES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadRides(String status) {
        setLoading(true);
        apiService.getAdminRides(status).enqueue(new Callback<List<RideHistoryItem>>() {
            @Override
            public void onResponse(Call<List<RideHistoryItem>> call, Response<List<RideHistoryItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<RideHistoryItem> rides = response.body();
                    adapter.setItems(rides);
                    binding.tvNoAdminRides.setVisibility(rides.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.something_went_wrong));
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
        Intent i = new Intent(this, AdminRideDetailActivity.class);
        i.putExtra(EXTRA_RIDE_ID, item.getId());
        startActivity(i);
    }


    private void setLoading(boolean loading) {
        binding.progressAdminRides.setVisibility(loading ? View.VISIBLE : View.GONE);
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
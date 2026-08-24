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
import com.example.team20_tim_djordjina.adapters.ProfileChangeAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminProfileChangesBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.ProfileChangeRequestItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileChangesActivity extends AppCompatActivity
        implements ProfileChangeAdapter.OnChangeActionListener {

    private ActivityAdminProfileChangesBinding binding;
    private ApiService apiService;
    private ProfileChangeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile_changes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminProfileChangesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new ProfileChangeAdapter(this);
        binding.rvChanges.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChanges.setAdapter(adapter);

        loadChanges();
    }

    private void loadChanges() {
        setLoading(true);
        apiService.getPendingProfileChanges().enqueue(new Callback<List<ProfileChangeRequestItem>>() {
            @Override
            public void onResponse(Call<List<ProfileChangeRequestItem>> call, Response<List<ProfileChangeRequestItem>> response) {
                setLoading(false);
                if(response.isSuccessful() && response.body() != null) {
                    adapter.setItems(response.body());
                    binding.tvNoChanges.setVisibility(
                            response.body().isEmpty() ? View.VISIBLE : View.GONE
                    );
                } else {
                    toast(getString(R.string.could_not_load_changes));
                }
            }

            @Override
            public void onFailure(Call<List<ProfileChangeRequestItem>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    @Override
    public void onApprove(ProfileChangeRequestItem item) {
        apiService.approveProfileChange(item.getId())
                .enqueue(new SimpleCallback(getString(R.string.change_approved)));
    }

    @Override
    public void onReject(ProfileChangeRequestItem item) {
        apiService.rejectProfileChange(item.getId())
                .enqueue(new SimpleCallback(getString(R.string.change_rejected)));
    }

    private class SimpleCallback implements Callback<ApiResponse> {

        private final String successMessage;
        SimpleCallback(String successMessage) { this.successMessage = successMessage; }

        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful()) {
                toast(successMessage);
                loadChanges();
            } else {
                toast(getString(R.string.something_went_wrong));
            }
        }

        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            toast(getString(R.string.network_error));
        }
    }


    private void setLoading(boolean loading) {
        binding.progressChanges.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
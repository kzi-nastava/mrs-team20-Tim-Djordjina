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
import com.example.team20_tim_djordjina.adapters.PanicAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminPanicBinding;
import com.example.team20_tim_djordjina.model.PanicItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Admin panic list (US#2.6.3)*/
public class AdminPanicActivity extends AppCompatActivity {

    private ActivityAdminPanicBinding binding;
    private ApiService apiService;
    private PanicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminPanicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new PanicAdapter();
        binding.rvPanics.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPanics.setAdapter(adapter);

        loadPanics();
    }

    private void loadPanics() {
        setLoading(true);
        apiService.getPanics().enqueue(new Callback<List<PanicItem>>() {
            @Override
            public void onResponse(Call<List<PanicItem>> call, Response<List<PanicItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PanicItem> panics = response.body();
                    adapter.setItems(panics);
                    binding.tvNoPanics.setVisibility(panics.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<List<PanicItem>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });

    }

    private void setLoading(boolean loading) {
        binding.progressPanics.setVisibility(loading ? View.VISIBLE : View.GONE);
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
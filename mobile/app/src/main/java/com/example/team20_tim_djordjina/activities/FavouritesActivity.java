package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
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
import com.example.team20_tim_djordjina.adapters.FavouriteRouteAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityFavouritesBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.FavouriteRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavouritesActivity extends AppCompatActivity
        implements FavouriteRouteAdapter.OnDeleteListener {

    private ActivityFavouritesBinding binding;
    private ApiService apiService;
    private FavouriteRouteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityFavouritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new FavouriteRouteAdapter(this);
        binding.rvFavourites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavourites.setAdapter(adapter);

        loadFavourites();
    }

    private void loadFavourites() {
        setLoading(true);
        apiService.getFavouriteRoutes().enqueue(new Callback<List<FavouriteRoute>>() {
            @Override
            public void onResponse(Call<List<FavouriteRoute>> call, Response<List<FavouriteRoute>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<FavouriteRoute> routes = response.body();
                    adapter.setItems(routes);
                    binding.tvNoFavourites.setVisibility(
                            routes.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.could_not_load_favourites));
                }
            }

            @Override
            public void onFailure(Call<List<FavouriteRoute>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    @Override
    public void onDelete(FavouriteRoute route) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_favourite)
                .setMessage(R.string.remove_favourite_confirm)
                .setPositiveButton(R.string.remove, (d, w) -> deleteFavourite(route))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteFavourite(FavouriteRoute route) {
        apiService.deleteFavouriteRoute(route.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    loadFavourites();
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                toast(getString(R.string.network_error));
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressFavourites.setVisibility(loading ? View.VISIBLE : View.GONE);
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
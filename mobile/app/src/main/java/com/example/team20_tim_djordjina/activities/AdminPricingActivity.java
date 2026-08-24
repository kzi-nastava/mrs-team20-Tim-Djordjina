package com.example.team20_tim_djordjina.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminPricingBinding;
import com.example.team20_tim_djordjina.model.PricingConfig;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPricingActivity extends AppCompatActivity {

    private ActivityAdminPricingBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_pricing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminPricingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        binding.btnSavePricing.setOnClickListener(v -> save());
        loadPricing();
    }

    private void loadPricing() {
        setLoading(true);
        apiService.getPricing().enqueue(new Callback<PricingConfig>() {
            @Override
            public void onResponse(Call<PricingConfig> call, Response<PricingConfig> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    fill(response.body());
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<PricingConfig> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void fill(PricingConfig p) {
        binding.etStandardPrice.setText(String.valueOf(p.getStandardPrice()));
        binding.etLuxuryPrice.setText(String.valueOf(p.getLuxuryPrice()));
        binding.etVanPrice.setText(String.valueOf(p.getVanPrice()));
        binding.etPricePerKm.setText(String.valueOf(p.getPricePerKm()));
    }

    private void save() {
        Double std = parseToDouble(binding.etStandardPrice);
        Double lux = parseToDouble(binding.etLuxuryPrice);
        Double van = parseToDouble(binding.etVanPrice);
        Double perKm = parseToDouble(binding.etPricePerKm);

        if (hasInvalidPrices(std, lux, van, perKm)) {
            toast(getString(R.string.enter_valid_prices));
            return;
        }
        if (existsNegativePrice(std, lux, van, perKm)) {
            toast(getString(R.string.prices_non_negative));
            return;
        }

        setLoading(true);
        apiService.updatePricing(new PricingConfig(std, lux, van, perKm))
                .enqueue(new Callback<PricingConfig>() {
                    @Override
                    public void onResponse(Call<PricingConfig> call, Response<PricingConfig> response) {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            toast(getString(R.string.pricing_saved));
                            finish();
                        } else {
                            toast(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<PricingConfig> call, Throwable t) {
                        setLoading(false);
                        toast(getString(R.string.network_error));
                    }
                });
    }

    private static boolean hasInvalidPrices(Double std, Double lux, Double van, Double perKm) {
        return std == null || lux == null || van == null || perKm == null;
    }

    private static boolean existsNegativePrice(Double std, Double lux, Double van, Double perKm) {
        return std < 0 || lux < 0 || van < 0 || perKm < 0;
    }

    private Double parseToDouble(EditText field) {
        String s = field.getText() != null ? field.getText().toString().trim() : "";
        if (TextUtils.isEmpty(s)) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String errorMessage(int code) {
        switch (code) {
            case 403: return getString(R.string.not_authorized);
            case 400: return getString(R.string.enter_valid_prices);
            default: return getString(R.string.something_went_wrong);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressPricing.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSavePricing.setEnabled(!loading);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
package com.example.team20_tim_djordjina.activities;

import android.app.DatePickerDialog;
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
import com.example.team20_tim_djordjina.adapters.AdminRideHistoryAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminUserHistoryBinding;
import com.example.team20_tim_djordjina.model.AdminRideHistoryItem;
import com.example.team20_tim_djordjina.model.AdminUserItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserHistoryActivity extends AppCompatActivity
    implements AdminRideHistoryAdapter.OnClick {

    public static final String EXTRA_RIDE_ID = "ride_id";
    private ActivityAdminUserHistoryBinding binding;
    private ApiService apiService;
    private AdminRideHistoryAdapter adapter;
    private final List<AdminUserItem> users = new ArrayList<>();
    private String fromIso = null;
    private String toIso = null;
    private Long selectedUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/
        binding = ActivityAdminUserHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        adapter = new AdminRideHistoryAdapter(this);
        binding.rvUserHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUserHistory.setAdapter(adapter);

        binding.btnForm.setOnClickListener(v -> pickDate(true));
        binding.btnTo.setOnClickListener(v -> pickDate(false));
        binding.btnClearDates.setOnClickListener(v -> clearDates());

        setupSortSpinner();
        loadUsers();
    }

    private void clearDates() {
        fromIso = null;
        toIso = null;
        binding.btnForm.setText(R.string.date_from);
        binding.btnTo.setText(R.string.date_to);
        loadHistory();
    }

    private void loadUsers() {
        apiService.getUsers().enqueue(new Callback<List<AdminUserItem>>() {
            @Override
            public void onResponse(Call<List<AdminUserItem>> call, Response<List<AdminUserItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    users.addAll(response.body());
                    setupUserSpinner();
                } else {
                    toast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<List<AdminUserItem>> call, Throwable t) {
                toast(getString(R.string.network_error));
            }
        });
    }

    private void setupUserSpinner() {
        List<String> labels = new ArrayList<>();
        for (AdminUserItem u : users) labels.add(u.label());
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels);
        binding.spinnerUser.setAdapter(a);
        binding.spinnerUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserId = users.get(position).getId();
                loadHistory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupSortSpinner() {
        String[] sorts = { "Newest", "Fare (high->low)", "Status" };
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, sorts);
        binding.spinnerSort.setAdapter(a);
        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applySort(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void pickDate(boolean isFrom) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String iso = String.format(Locale.US, "%04d-%02d-%02dT%s",
                    year, month + 1, day, isFrom ? "00:00:00" : "23:59:59");
            if (isFrom)  {
                fromIso = iso;
                binding.btnForm.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day));
            } else {
                toIso = iso;
                binding.btnTo.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day));
            }
            loadHistory();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadHistory() {
        if (selectedUserId == null) return;
        setLoading(true);
        apiService.getAdminUserHistory(selectedUserId, fromIso, toIso)
                .enqueue(new Callback<List<AdminRideHistoryItem>>() {
                    @Override
                    public void onResponse(Call<List<AdminRideHistoryItem>> call, Response<List<AdminRideHistoryItem>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setItems(response.body());
                            applySort(binding.spinnerSort.getSelectedItemPosition());
                            binding.tvNoUserHistory.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            toast(getString(R.string.something_went_wrong));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AdminRideHistoryItem>> call, Throwable t) {
                        setLoading(false);
                        toast(getString(R.string.network_error));
                    }
                });
    }

    private void applySort(int mode) {
        List<AdminRideHistoryItem> list = adapter.getItems();
        switch (mode) {
            case 1:
                Collections.sort(list, (a, b) -> Double.compare(b.getRide().getFare(), a.getRide().getFare()));
                break;
            case 2:
                Collections.sort(list, (a, b) -> a.getRide().getStatus().compareTo(b.getRide().getStatus()));
                break;
            default:
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void setLoading(boolean loading) {
        binding.progressUserHistory.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(AdminRideHistoryItem item) {
        Intent intent = new Intent(this, AdminUserHistoryDetailActivity.class);
        intent.putExtra(EXTRA_RIDE_ID, item.getRide().getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


}
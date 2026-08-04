package com.example.team20_tim_djordjina.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.adapters.NotificationAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityNotificationsBinding;
import com.example.team20_tim_djordjina.model.ApiResponse;
import com.example.team20_tim_djordjina.model.NotificationItem;
import com.example.team20_tim_djordjina.util.NotificationHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Notification list.
 * Loads GET /api/notifications;
 * Shows a system notification for any new unread ones;
 * Marks a notification read on top. */
public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    public static final int REQ_POST_NOTIFICATIONS = 101;
    private ActivityNotificationsBinding binding;
    private ApiService apiService;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getInstance(this).getApiService();

        NotificationHelper.createChannel(this);
        requestPostNotificationsPermissionIfNeeded();

        adapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void requestPostNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIFICATIONS);
            }
        }
    }

    private void loadNotifications() {
        setLoading(true);
        apiService.getNotifications().enqueue(new Callback<List<NotificationItem>>() {
            @Override
            public void onResponse(Call<List<NotificationItem>> call, Response<List<NotificationItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationItem> items = response.body();
                    adapter.setItems(items);
                    binding.tvNoNotifications.setVisibility(
                            items.isEmpty() ? View.VISIBLE : View.GONE);

                    for (NotificationItem item : items) {
                        NotificationHelper.showIfNew(NotificationsActivity.this, item);
                    }
                } else {
                    toast(getString(R.string.could_not_load_notifications));
                }
            }

            @Override
            public void onFailure(Call<List<NotificationItem>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });

    }

    @Override
    public void onNotificationClick(NotificationItem item) {
        apiService.markNotificationRead(item.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    loadNotifications();
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
        binding.progressNotifications.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        binding = null;
    }
}
package com.example.team20_tim_djordjina.activities;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.adapters.UserListAdapter;
import com.example.team20_tim_djordjina.api.ApiService;
import com.example.team20_tim_djordjina.api.RetrofitClient;
import com.example.team20_tim_djordjina.databinding.ActivityAdminUserListBinding;
import com.example.team20_tim_djordjina.model.BlockUserRequest;
import com.example.team20_tim_djordjina.model.UserListItem;
import com.example.team20_tim_djordjina.util.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* Admin screen listing passengers and drivers, with block / unblock actions */
public class AdminUserListActivity extends AppCompatActivity implements UserListAdapter.OnUserActionListener {

    private ActivityAdminUserListBinding binding;
    private ApiService apiService;
    private UserListAdapter adapter;

    // null = all, "USER" = passengers, "DRIVER" = drivers
    private String roleFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */
        binding = ActivityAdminUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getInstance(this).getApiService();
        TokenManager tokenManager = new TokenManager(this);

        if (!"ADMIN".equals(tokenManager.getRole())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.not_allowed)
                    .setMessage(R.string.only_admins_manage_users)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (d,w) -> finish())
                    .show();
            return;
        }

        adapter = new UserListAdapter(this);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        binding.btnFilterAll.setOnClickListener(v -> applyFilter(null));
        binding.btnFilterPassengers.setOnClickListener(v -> applyFilter("USER"));
        binding.btnFilterDrivers.setOnClickListener(v -> applyFilter("DRIVER"));

        loadUsers();
    }

    private void applyFilter(String role){
        roleFilter = role;
        loadUsers();
    }

    private void loadUsers(){
        setLoading(true);
        apiService.listUsers(roleFilter).enqueue(new Callback<List<UserListItem>>() {
            @Override
            public void onResponse(Call<List<UserListItem>> call, Response<List<UserListItem>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body());
                    binding.tvEmpty.setVisibility(
                            response.body().isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    toast(getString(R.string.could_not_load_users));
                }
            }

            @Override
            public void onFailure(Call<List<UserListItem>> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    // -------------------- Adapter callbacks --------------------

    @Override
    public void onBlockClicked(UserListItem user) {
        EditText input = new EditText(this);
        input.setHint(R.string.block_note_hint);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.block_user_title, user.getFullName()))
                .setView(input)
                .setPositiveButton(R.string.block, (d, w) -> {
                    String note = input.getText().toString().trim();
                    if (TextUtils.isEmpty(note)){
                        toast(getString(R.string.block_note_required));
                        return;
                    }
                    blockUser(user, note);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onUnblockClicked(UserListItem user) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.unblock_user_title, user.getFullName()))
                .setMessage(R.string.unblock_confirm)
                .setPositiveButton(R.string.unblock, (d, w) -> unblockUser(user))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // -------------------- API Calls --------------------
    private void blockUser(UserListItem user, String note){
        setLoading(true);
        apiService.blockUser(user.getId(), new BlockUserRequest(note)).enqueue(new Callback<UserListItem>() {
            @Override
            public void onResponse(Call<UserListItem> call, Response<UserListItem> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    toast(getString(R.string.user_blocked));
                    loadUsers();
                } else {
                    toast(getString(R.string.could_not_block_user));
                }
            }

            @Override
            public void onFailure(Call<UserListItem> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    private void unblockUser(UserListItem user) {
        setLoading(true);
        apiService.unblockUser(user.getId()).enqueue(new Callback<UserListItem>() {
            @Override
            public void onResponse(Call<UserListItem> call, Response<UserListItem> response) {
                setLoading(false);
                if (response.isSuccessful()){
                    toast(getString(R.string.user_unblocked));
                    loadUsers();
                } else {
                    toast(getString(R.string.could_not_unblock_user));
                }
            }

            @Override
            public void onFailure(Call<UserListItem> call, Throwable t) {
                setLoading(false);
                toast(getString(R.string.network_error));
            }
        });
    }

    // -------------------- Helpers --------------------
    private void setLoading(boolean loading){
        binding.progressUsers.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
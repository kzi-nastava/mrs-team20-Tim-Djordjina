package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.UserListItem;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onBlockClicked(UserListItem user);
        void onUnblockClicked(UserListItem user);
    }

    private final List<UserListItem> users = new ArrayList<>();
    private final OnUserActionListener listener;

    public UserListAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserListItem> newUsers){
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }




    static class UserViewHolder extends RecyclerView.ViewHolder {

        public static final int COLOR_RED = 0xFFD32F2F;
        public static final int COLOR_GREEN = 0xFF2E7D32;
        private final TextView tvName;
        private final TextView tvEmail;
        private final TextView tvRole;
        private final TextView tvStatus;
        private final TextView tvBlockNote;
        private final Button btnToggleBlock;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            tvBlockNote = itemView.findViewById(R.id.tvUserBlockNote);
            btnToggleBlock = itemView.findViewById(R.id.btnToggleBlock);
        }

        void bind(UserListItem user, OnUserActionListener listener) {
            tvName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole());

            if (user.isBlocked()) {
                tvStatus.setText(R.string.status_blocked);
                tvStatus.setTextColor(COLOR_RED);  // red
                btnToggleBlock.setText(R.string.unblock);
                btnToggleBlock.setOnClickListener(v -> listener.onUnblockClicked(user));

                if (user.getBlockNote() != null && !user.getBlockNote().isEmpty()){
                    tvBlockNote.setText(user.getBlockNote());
                    tvBlockNote.setVisibility(View.VISIBLE);
                } else {
                    tvBlockNote.setVisibility(View.GONE);
                }
            } else {
                tvStatus.setText(R.string.status_active);
                tvStatus.setTextColor(COLOR_GREEN);  // green
                tvBlockNote.setVisibility(View.GONE);
                btnToggleBlock.setText(R.string.block);
                btnToggleBlock.setOnClickListener(v -> listener.onBlockClicked(user));
            }
        }
    }
}

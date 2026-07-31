package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.ProfileChangeRequestItem;

import java.util.ArrayList;
import java.util.List;

public class ProfileChangeAdapter extends RecyclerView.Adapter<ProfileChangeAdapter.ChangeViewHolder> {

    public interface OnChangeActionListener {
        void onApprove(ProfileChangeRequestItem item);
        void onReject(ProfileChangeRequestItem item);
    }

    private final List<ProfileChangeRequestItem> items = new ArrayList<>();
    private final OnChangeActionListener listener;

    public ProfileChangeAdapter(OnChangeActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProfileChangeRequestItem> newItems) {
        items.clear();
        if(newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_change, parent, false);
        return new ChangeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChangeViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class ChangeViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDriverEmail;
        private final TextView tvProposed;
        private final Button btnApprove;
        private final Button btnReject;

        ChangeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverEmail = itemView.findViewById(R.id.tvChangeDriverEmail);
            tvProposed = itemView.findViewById(R.id.tvChangeProposed);
            btnApprove = itemView.findViewById(R.id.btnApproveChange);
            btnReject = itemView.findViewById(R.id.btnRejectChange);
        }

        void bind(ProfileChangeRequestItem item, OnChangeActionListener listener) {
            tvDriverEmail.setText(item.getDriverEmail());
            String proposed = item.getFullName().trim()
                    + "\n" + item.getPhoneNumber()
                    + "\n" + item.getAddress();
            tvProposed.setText(proposed);
            btnApprove.setOnClickListener(v -> listener.onApprove(item));
            btnReject.setOnClickListener(v -> listener.onReject(item));
        }

    }
}

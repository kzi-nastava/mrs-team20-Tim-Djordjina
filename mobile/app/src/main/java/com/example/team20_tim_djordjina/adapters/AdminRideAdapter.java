package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.RideHistoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRideAdapter
        extends RecyclerView.Adapter<AdminRideAdapter.AdminRideViewHolder>{

    public interface OnRideClickListener {
        void onRideClick(RideHistoryItem item);
    }

    private final List<RideHistoryItem> items  = new ArrayList<>();
    private final OnRideClickListener listener;

    public AdminRideAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RideHistoryItem> newItems){
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_ride, parent, false);
        return new AdminRideViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminRideViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class AdminRideViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvStatus, tvFare;
        public AdminRideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvAdminRideRoute);
            tvStatus = itemView.findViewById(R.id.tvAdminRideStatus);
            tvFare = itemView.findViewById(R.id.tvAdminRideFare);
        }

        void bind(RideHistoryItem item, OnRideClickListener listener) {
            tvRoute.setText("#" + item.getId() + "  " +
                    item.getPickupAddress() + " → " + item.getDestinationAddress());
            tvStatus.setText(item.getStatus());
            tvFare.setText(String.format(Locale.US, "%.2f RSD", item.getFare()));
            itemView.setOnClickListener(v -> listener.onRideClick(item));
        }
    }


}

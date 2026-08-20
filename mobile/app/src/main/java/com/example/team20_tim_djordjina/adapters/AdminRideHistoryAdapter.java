package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.AdminRideHistoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRideHistoryAdapter
        extends RecyclerView.Adapter<AdminRideHistoryAdapter.VH> {

    public interface OnClick {
        void onClick(AdminRideHistoryItem item);
    }

    private final List<AdminRideHistoryItem> items = new ArrayList<>();
    private final OnClick listener;
    public AdminRideHistoryAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminRideHistoryItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        // TODO notifyDataSetChanged();
    }

    public List<AdminRideHistoryItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user_ride, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvMeta, tvRating;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvAurRoute);
            tvMeta = itemView.findViewById(R.id.tvAurMeta);
            tvRating = itemView.findViewById(R.id.tvAurRating);
        }

        void bind(AdminRideHistoryItem item, OnClick listener) {
            tvRoute.setText(item.getRide().getPickupAddress() + " -> "
                    + item.getRide().getDestinationAddress());

            StringBuilder meta = new StringBuilder();
            meta.append(item.getRide().getStatus());
            String date = item.getRide().getDisplayDate();
            if (date != null && !date.isEmpty()) meta.append(" · ").append(date);
            meta.append(String.format(Locale.US, " · %.0f RSD", item.getRide().getFare()));
            if (item.isCancelled()) meta.append(" · CANCELLED");
            if (item.isPanicTriggered()) meta.append(" · PANIC");
            tvMeta.setText(meta.toString());

            if (item.getDriverRating() != null) {
                tvRating.setText(String.format(Locale.US, "★ driver %d · vehicle %d",
                        item.getDriverRating(),
                        item.getVehicleRating() != null ? item.getVehicleRating() : 0));
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onClick(item));
        }
    }
}

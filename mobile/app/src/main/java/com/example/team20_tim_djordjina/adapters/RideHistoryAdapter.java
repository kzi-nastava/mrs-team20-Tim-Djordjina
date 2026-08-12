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

public class RideHistoryAdapter
        extends RecyclerView.Adapter<RideHistoryAdapter.HistoryViewHolder> {

    public interface OnRideClickListener {
        void onRideClick(RideHistoryItem item);
    }

    private final List<RideHistoryItem> items = new ArrayList<>();
    private final OnRideClickListener listener;
    public RideHistoryAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<RideHistoryItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);

    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvDate, tvFare;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvHistoryRoute);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvFare = itemView.findViewById(R.id.tvHistoryFare);
        }

        void bind(RideHistoryItem item, OnRideClickListener listener) {
            tvRoute.setText(item.getPickupAddress() + " -> " + item.getDestinationAddress());
            tvDate.setText(item.getDisplayDate());
            tvFare.setText(String.format(Locale.US, "%.2f RSD", item.getFare()));
            itemView.setOnClickListener(v -> listener.onRideClick(item));
        }
    }
}

package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.RideResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LinkedRideAdapter extends RecyclerView.Adapter<LinkedRideAdapter.LinkedRideViewHolder> {

    private final List<RideResponse> items = new ArrayList<>();

    public void setItems(List<RideResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LinkedRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_linked_ride, parent, false);
        return new LinkedRideViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkedRideViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LinkedRideViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRoute, tvStatus, tvDriver, tvFare;

        LinkedRideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvLinkedRoute);
            tvStatus = itemView.findViewById(R.id.tvLinkedStatus);
            tvDriver = itemView.findViewById(R.id.tvLinkedDriver);
            tvFare = itemView.findViewById(R.id.tvLinkedFare);
        }

        void bind(RideResponse r) {
            tvRoute.setText(r.getPickupAddress() + " -> " + r.getDestinationAddress());
            tvStatus.setText(itemView.getContext().getString(R.string.linked_status, r.getStatus()));

            if (r.getDriver() != null) {
                tvDriver.setText(itemView.getContext().getString(R.string.linked_driver,
                        r.getDriver().getFirstName() + " " + r.getDriver().getLastName()));
            } else {
                tvDriver.setText(R.string.linked_driver_none);
            }

            tvFare.setText(String.format(Locale.US, "%.2f RSD", r.getFare()));
        }
    }
}

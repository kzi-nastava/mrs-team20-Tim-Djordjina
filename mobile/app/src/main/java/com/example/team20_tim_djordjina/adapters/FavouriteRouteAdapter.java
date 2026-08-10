package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.FavouriteRoute;

import java.util.ArrayList;
import java.util.List;

public class FavouriteRouteAdapter
        extends RecyclerView.Adapter<FavouriteRouteAdapter.FavouriteViewHolder> {

    @NonNull
    @Override
    public FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite_route, parent, false);
        return new FavouriteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnDeleteListener {
        void onDelete(FavouriteRoute route);
    }

    private final List<FavouriteRoute> items = new ArrayList<>();
    private final OnDeleteListener listener;

    public FavouriteRouteAdapter(OnDeleteListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FavouriteRoute> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);

    }


    static class FavouriteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLabel, tvRoute;
        private final ImageButton btnDelete;
        FavouriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvFavLabel);
            tvRoute = itemView.findViewById(R.id.tvFavRoute);
            btnDelete = itemView.findViewById(R.id.btnFavDelete);
        }

        void bind(FavouriteRoute route, OnDeleteListener listener) {
            String label = route.getLabel() != null && !route.getLabel().isEmpty()
                    ? route.getLabel() : route.getPickupAddress();
            tvLabel.setText(label);
            tvRoute.setText(route.getPickupAddress() + " -> " + route.getDestinationAddress());
            btnDelete.setOnClickListener(v -> listener.onDelete(route));
        }
    }
}

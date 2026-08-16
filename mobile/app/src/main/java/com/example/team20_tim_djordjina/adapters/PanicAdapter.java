package com.example.team20_tim_djordjina.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.PanicItem;

import java.util.ArrayList;
import java.util.List;

public class PanicAdapter extends RecyclerView.Adapter<PanicAdapter.PanicViewHolder> {

    private final List<PanicItem> items = new ArrayList<>();

    public void setItems(List<PanicItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PanicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panic, parent, false);
        return new PanicViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PanicViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class PanicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWho, tvMeta, tvNote;

        public PanicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWho = itemView.findViewById(R.id.tvPanicWho);
            tvMeta = itemView.findViewById(R.id.tvPanicMeta);
            tvNote = itemView.findViewById(R.id.tvPanicNote);
        }

        void bind(PanicItem p) {
            tvWho.setText("PANIC · ride #" + p.getRideId());
            String who  = p.getTriggeredByName() + " (" + p.getTriggeredByRole() + ")";
            tvMeta.setText(who + " · " + p.getRideStatus() + " · " + p.getDisplayDate());
            if (p.getNote() != null && !p.getNote().isEmpty()) {
                tvNote.setText(p.getNote());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }
        }
    }
}

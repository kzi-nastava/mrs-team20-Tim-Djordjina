package com.example.team20_tim_djordjina.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team20_tim_djordjina.R;
import com.example.team20_tim_djordjina.model.NotificationItem;

import java.util.ArrayList;
import java.util.List;

/** List notifications; unread ones are emphasised. Tapping one marks it read. */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
    }
    private final List<NotificationItem> items = new ArrayList<>();
    private final OnNotificationClickListener listener;
    public NotificationAdapter(OnNotificationClickListener listener){
        this.listener = listener;
    }

    public void setItems(List<NotificationItem> newItems) {
        items.clear();
        if(newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final View unreadDot;
        private final TextView tvMessage;
        private final TextView tvTime;


        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            unreadDot = itemView.findViewById(R.id.viewUnreadDot);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
        }

        void bind(NotificationItem item, OnNotificationClickListener listener) {
            tvMessage.setText(item.getMessage());
            tvTime.setText(item.getDisplayTime());

            if(item.isRead()) {
                unreadDot.setVisibility(View.INVISIBLE);
                tvMessage.setTypeface(null, Typeface.NORMAL);
            } else {
                unreadDot.setVisibility(View.VISIBLE);
                tvMessage.setTypeface(null, Typeface.BOLD);
            }

            itemView.setOnClickListener(v -> {
                if (!item.isRead()) listener.onNotificationClick(item);
            });
        }
    }
}

package com.example.team20_tim_djordjina.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.team20_tim_djordjina.model.NotificationItem;

import java.util.HashSet;
import java.util.Set;

/** Android system notifications + SharedPreferences state */
public class NotificationHelper {

    private static final String CHANNEL_ID = "rideon_notifications";
    private static final String CHANNEL_NAME = "RideOn notifications";
    private static final String PREFS = "notification_prefs";
    private static final String KEY_ENABLED = "notifications_enabled";
    private static final String KEY_SHOWN_IDS = "shown_ids";

    private NotificationHelper() {}

    /** Create the channel once (Required Android 8.0+) */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Ride and account notifications");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean areNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, true);
    }
    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    private static boolean wasShown(Context context, long id) {
        Set<String> shown = prefs(context).getStringSet(KEY_SHOWN_IDS, new HashSet<>());
        return shown.contains(String.valueOf(id));
    }

    private static void markShown(Context context, long id) {
        Set<String> shown = new HashSet<>(
                prefs(context).getStringSet(KEY_SHOWN_IDS, new HashSet<>()));
        shown.add(String.valueOf(id));
        prefs(context).edit().putStringSet(KEY_SHOWN_IDS, shown).apply();
    }

    /** Shows a system notification for this item if it's unread, notifications are
     *  enabled, and it hasn't been shown before. */
    public static void showIfNew(Context context, NotificationItem item) {
        if (item == null || item.getId() == null) return;
        if (!areNotificationsEnabled(context)) return;
        if (item.isRead()) return;
        if (wasShown(context, item.getId())) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titleFor(item.getType()))
                .setContentText(item.getMessage())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(item.getMessage()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify(item.getId().intValue(), builder.build());
        } catch (SecurityException ignored) {

        }
    }

    private static String titleFor(String type) {
        if (type == null) return "RideOn";
        switch (type) {
            case "NEW_RIDE": return "New ride";
            case "RIDE_ACCEPTED": return "Ride accepted";
            case "RIDE_FAILED": return "No drivers available";
            case "RIDE_FINISHED": return "Ride finished";
            default: return "RideOn";
        }
    }

}

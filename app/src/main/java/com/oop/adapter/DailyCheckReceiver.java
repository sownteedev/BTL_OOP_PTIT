package com.oop.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.oop.R;
import com.oop.MainActivity;

public class DailyCheckReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "daily_check_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        double dailyIncome = intent.getDoubleExtra("dailyIncome", 0);
        double dailyExpense = intent.getDoubleExtra("dailyExpense", 0);

        if (dailyIncome == 0 && dailyExpense == 0) {
            showNoTransactionNotification(context);
        } else {
            showNotification(context, dailyIncome, dailyExpense);
        }
    }

    private void showNotification(Context context, double dailyIncome, double dailyExpense) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_monetization_on_24)
                .setContentTitle("Daily Wallet Check")
                .setContentText("Income: " + dailyIncome + ", Expense: " + dailyExpense)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }

    private void showNoTransactionNotification(Context context) {
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_outline_monetization_on_24)
                .setContentTitle("Daily Wallet Check")
                .setContentText("You haven't updated your wallet today")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Check Notification";
            String description = "Channel for daily wallet check notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
package com.oop.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.oop.R;
import com.oop.MainActivity;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        double dailyIncome = getInputData().getDouble("dailyIncome", 0);
        double dailyExpense = getInputData().getDouble("dailyExpense", 0);
        double balance = getInputData().getDouble("balance", 0);

        if (dailyIncome == 0 && dailyExpense == 0) {
            showNoTransactionNotification();
        } else {
            showNotification(dailyIncome, dailyExpense, balance);
        }

        return Result.success();
    }

    private void showNotification(double dailyIncome, double dailyExpense, double balance) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("daily_check", "Biến động số dư", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_check")
                .setContentTitle("Biến động số dư")
                .setContentText("Tiền vào: " + dailyIncome + ", Tiền ra: " + dailyExpense + ", Số dư: " + balance)
                .setSmallIcon(R.drawable.ic_outline_monetization_on_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.notify(1, builder.build());
    }

    private void showNoTransactionNotification() {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("daily_check", "Biến động số dư", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_check")
                .setContentTitle("Biến động số dư")
                .setContentText("Bạn chưa cập nhật ví tiền của mình hôm nay")
                .setSmallIcon(R.drawable.ic_outline_monetization_on_24)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.notify(1, builder.build());
    }
}
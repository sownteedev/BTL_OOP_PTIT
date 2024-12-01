package com.oop.adapter;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.oop.db.oop.CashFlowDatabase;
import com.oop.db.oop.CashItem;
import com.oop.helper.CashFlowHelper;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        if ("com.mbmobile".equals(packageName)) {
            String notificationTitle = sbn.getNotification().extras.getString("android.title");
            String notificationText = sbn.getNotification().extras.getString("android.text");

            Log.d(TAG, "Notification from MB Bank: " + notificationTitle + " - " + notificationText);

            if (notificationText != null) {
                processNotification(notificationText);
            }
        }
    }

    private void processNotification(String notificationText) {
        try {
            String[] parts = notificationText.split("\\|");
            String transactionPart = parts[1].trim(); // GD: -5,000VND 30/11/24 15:58
            String balancePart = parts[2].trim(); // SD: 1,839,543VND
            String descriptionPart = parts[3].trim(); // ND: LE DUC HIEU chuyen tien

            // Extract amount and date-time from transactionPart
            String[] transactionDetails = transactionPart.split(" ");
            String amountString = transactionDetails[1]; // -5,000VND
            String dateTimeString = transactionDetails[2] + " " + transactionDetails[3]; // 30/11/24 15:58

            // Extract description
            String description = descriptionPart.substring(4); // LE DUC HIEU chuyen tien

            // Parse amount
            amountString = amountString.replace("VND", "").replace(",", "");
            double amount = Double.parseDouble(amountString);

            // Determine type
            String type = amountString.startsWith("-") ? "debit" : "credit";

            // Add transaction to database
            addTransactionToDatabase(amount, description, type, dateTimeString);
        } catch (Exception e) {
            Log.e(TAG, "Failed to process notification: " + e.getMessage());
        }
    }

    private void addTransactionToDatabase(double amount, String description, String type, String dateTime) {
        CashItem cashItem = new CashItem();
        cashItem.setAmount(amount);
        cashItem.setDesc(description);
        cashItem.setType(type);
        cashItem.setTime(System.currentTimeMillis()); // You can parse dateTime to a timestamp if needed

        CashFlowDatabase database = CashFlowHelper.getDatabaseInstance(this);
        database.getCashFlowDao().addItem(cashItem);

        Log.d(TAG, "Added " + type + " to database: " + amount + " - " + description + " at " + dateTime);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
    }
}
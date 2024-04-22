package com.mawd.to_do.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.mawd.to_do.MainActivity;
import com.mawd.to_do.R;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = (int) System.currentTimeMillis();
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE);
        String contentText = "A task is due today!";
        // Check if the app has the required permission to vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "todo_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Reminder")
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingI);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(notificationId, builder.build());
        } else {
            Log.e("notification_log", "Permission is required");
            // Handle the case where the app does not have the required permission
            // You can request the permission here or take appropriate action
        }
    }
}

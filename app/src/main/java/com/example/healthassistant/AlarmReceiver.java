package com.example.healthassistant;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * 接收闹钟广播并弹出吃药提醒通知
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "MEDICINE_REMINDER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        int planId = intent.getIntExtra("plan_id", -1);
        int medicationId = intent.getIntExtra("medication_id", -1);
        String scheduledAt = intent.getStringExtra("scheduled_at");
        String dosage = intent.getStringExtra("dose");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "用药提醒",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("提醒您准时服药");
            notificationManager.createNotificationChannel(channel);
        }

        // 点击通知跳转到主界面
        Intent mainIntent = new Intent(context, ReminderActionActivity.class);
        mainIntent.putExtra("plan_id", planId);
        mainIntent.putExtra("medication_id", medicationId);
        mainIntent.putExtra("scheduled_at", scheduledAt);
        mainIntent.putExtra("dose", dosage);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                mainIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("用药提醒")
                .setContentText("点击记录已服/延后/跳过")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

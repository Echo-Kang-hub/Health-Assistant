package com.example.healthassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

/**
 * 管理闹钟的设置和取消
 */
public class ReminderManager {

    public static void setReminder(Context context, Medicine medicine) {
        if (!medicine.isReminderEnabled() || medicine.getReminderTime().isEmpty()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // 关键修复：处理 Android 12+ 的精确闹钟权限导致的闪退
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 如果没有精确闹钟权限，则降级使用非精确闹钟，防止崩溃
                scheduleInexactAlarm(context, alarmManager, medicine);
                Toast.makeText(context, "由于缺少精确闹钟权限，提醒可能会有几分钟延迟", Toast.LENGTH_LONG).show();
                return;
            }
        }

        scheduleExactAlarm(context, alarmManager, medicine);
    }

    private static void scheduleExactAlarm(Context context, AlarmManager alarmManager, Medicine medicine) {
        PendingIntent pendingIntent = createPendingIntent(context, medicine);
        long triggerTime = calculateTriggerTime(medicine);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private static void scheduleInexactAlarm(Context context, AlarmManager alarmManager, Medicine medicine) {
        PendingIntent pendingIntent = createPendingIntent(context, medicine);
        long triggerTime = calculateTriggerTime(medicine);
        // 降级为非精确闹钟
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private static long calculateTriggerTime(Medicine medicine) {
        String[] timeParts = medicine.getReminderTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }

    private static PendingIntent createPendingIntent(Context context, Medicine medicine) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicine_name", medicine.getName());
        intent.putExtra("dosage", medicine.getDosage());
        return PendingIntent.getBroadcast(
                context, 
                medicine.getId(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static void cancelReminder(Context context, Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, medicine);
        alarmManager.cancel(pendingIntent);
    }
}
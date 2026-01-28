package com.example.healthassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReminderScheduler {
    public static void scheduleNext(Context context, Plan plan, int medicationId) {
        if (plan == null) return;
        RetrofitClient.getApiService().getNextOccurrences(plan.getId(), new OccurrenceRequest(1))
                .enqueue(new Callback<ApiResponse<OccurrenceResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<OccurrenceResponse>> call, Response<ApiResponse<OccurrenceResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null
                                && response.body().getData().getItems() != null
                                && !response.body().getData().getItems().isEmpty()) {
                            String scheduledAt = response.body().getData().getItems().get(0);
                            scheduleAlarm(context, plan.getId(), medicationId, scheduledAt, plan.getDose());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<OccurrenceResponse>> call, Throwable t) {
                    }
                });
    }

    private static void scheduleAlarm(Context context, int planId, int medicationId, String scheduledAt, String dose) {
        long triggerAt = parseIsoToMillis(scheduledAt);
        if (triggerAt <= 0) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = buildPendingIntent(context, planId, medicationId, scheduledAt, dose);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static PendingIntent buildPendingIntent(Context context, int planId, int medicationId, String scheduledAt, String dose) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("plan_id", planId);
        intent.putExtra("medication_id", medicationId);
        intent.putExtra("scheduled_at", scheduledAt);
        intent.putExtra("dose", dose);
        int requestCode = planId * 1000 + medicationId;
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static long parseIsoToMillis(String iso) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = format.parse(iso);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

package com.example.healthassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReminderActionActivity extends AppCompatActivity {
    private int planId;
    private int medicationId;
    private String scheduledAt;
    private String dose;
    private TextView detailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_action);

        planId = getIntent().getIntExtra("plan_id", -1);
        medicationId = getIntent().getIntExtra("medication_id", -1);
        scheduledAt = getIntent().getStringExtra("scheduled_at");
        dose = getIntent().getStringExtra("dose");

        detailText = findViewById(R.id.text_reminder_detail);
        Button buttonTaken = findViewById(R.id.button_taken);
        Button buttonDelay = findViewById(R.id.button_delay);
        Button buttonSkip = findViewById(R.id.button_skip);

        buttonTaken.setOnClickListener(v -> logDose("TAKEN", false));
        buttonDelay.setOnClickListener(v -> logDose("DELAYED", true));
        buttonSkip.setOnClickListener(v -> logDose("SKIPPED", false));

        loadMedication();
    }

    private void loadMedication() {
        if (medicationId <= 0) {
            detailText.setText("剂量: " + (TextUtils.isEmpty(dose) ? "-" : dose));
            return;
        }
        RetrofitClient.getApiService().getMedication(medicationId)
                .enqueue(new Callback<ApiResponse<Medication>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Medication>> call, Response<ApiResponse<Medication>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Medication med = response.body().getData();
                            detailText.setText(med.getName() + " · " + (dose == null ? "" : dose));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Medication>> call, Throwable t) {
                        detailText.setText("剂量: " + (TextUtils.isEmpty(dose) ? "-" : dose));
                    }
                });
    }

    private void logDose(String status, boolean delay) {
        String takenAt = null;
        if ("TAKEN".equals(status) || "DELAYED".equals(status)) {
            takenAt = toIsoString(System.currentTimeMillis());
        }
        DoseLogRequest request = new DoseLogRequest(planId > 0 ? planId : null, medicationId, scheduledAt, takenAt, status, null);
        RetrofitClient.getApiService().createDoseLog(request)
                .enqueue(new Callback<ApiResponse<DoseLog>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DoseLog>> call, Response<ApiResponse<DoseLog>> response) {
                        if (delay) {
                            scheduleDelay();
                        } else {
                            scheduleNext();
                        }
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DoseLog>> call, Throwable t) {
                        Toast.makeText(ReminderActionActivity.this, "记录失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void scheduleDelay() {
        try {
            long triggerAt = parseIsoToMillis(scheduledAt) + (15 * 60 * 1000);
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            android.app.PendingIntent pendingIntent = ReminderScheduler.buildPendingIntent(
                    this,
                    planId,
                    medicationId,
                    toIsoString(triggerAt),
                    dose
            );
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }
            Toast.makeText(this, "已延后 15 分钟", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "无法延后", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseIsoToMillis(String iso) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = format.parse(iso);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private String toIsoString(long timeMillis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date(timeMillis));
    }

    private void scheduleNext() {
        if (planId <= 0) return;
        RetrofitClient.getApiService().getPlan(planId)
                .enqueue(new Callback<ApiResponse<Plan>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Plan>> call, Response<ApiResponse<Plan>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            ReminderScheduler.scheduleNext(ReminderActionActivity.this, response.body().getData(), medicationId);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Plan>> call, Throwable t) {
                    }
                });
    }
}

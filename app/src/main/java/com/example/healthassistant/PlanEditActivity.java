package com.example.healthassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanEditActivity extends AppCompatActivity {
    private int medicationId;
    private EditText inputFrequency;
    private EditText inputTimes;
    private EditText inputDose;
    private Switch switchWithMeal;
    private EditText inputStart;
    private EditText inputEnd;
    private TextView textAiResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_edit);

        medicationId = getIntent().getIntExtra("medication_id", -1);

        inputFrequency = findViewById(R.id.input_plan_frequency);
        inputTimes = findViewById(R.id.input_plan_times);
        inputDose = findViewById(R.id.input_plan_dose);
        switchWithMeal = findViewById(R.id.switch_plan_with_meal);
        inputStart = findViewById(R.id.input_plan_start);
        inputEnd = findViewById(R.id.input_plan_end);
        textAiResult = findViewById(R.id.text_plan_ai_result);
        Button buttonAi = findViewById(R.id.button_plan_ai);
        Button buttonSave = findViewById(R.id.button_plan_save);

        inputFrequency.setText(String.valueOf(getIntent().getIntExtra("plan_frequency", 1)));
        inputTimes.setText(getIntent().getStringExtra("plan_times"));
        inputDose.setText(getIntent().getStringExtra("plan_dose"));
        switchWithMeal.setChecked(getIntent().getBooleanExtra("plan_with_meal", false));
        inputStart.setText(getIntent().getStringExtra("plan_start"));
        inputEnd.setText(getIntent().getStringExtra("plan_end"));

        buttonAi.setOnClickListener(v -> recommendTimes());
        buttonSave.setOnClickListener(v -> savePlan());
    }

    private void recommendTimes() {
        int frequency = parseInt(inputFrequency.getText().toString().trim(), 1);
        String usageText = "每日 " + frequency + " 次";
        String timezone = TimeZone.getDefault().getID();
        PlanRecommendRequest request = new PlanRecommendRequest(null, usageText, frequency, timezone);
        RetrofitClient.getApiService().recommendTimes(request)
                .enqueue(new Callback<ApiResponse<PlanRecommendResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PlanRecommendResponse>> call, Response<ApiResponse<PlanRecommendResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            PlanRecommendResponse data = response.body().getData();
                            if (data.getTimes() != null) {
                                inputTimes.setText(TextUtils.join(",", data.getTimes()));
                                textAiResult.setText("推荐时段：" + TextUtils.join(",", data.getTimes()) + "\n理由：" + data.getReason());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PlanRecommendResponse>> call, Throwable t) {
                        Toast.makeText(PlanEditActivity.this, "推荐失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void savePlan() {
        if (medicationId <= 0) {
            Toast.makeText(this, "缺少药品信息", Toast.LENGTH_SHORT).show();
            return;
        }
        int frequency = parseInt(inputFrequency.getText().toString().trim(), 1);
        List<String> times = parseTimes(inputTimes.getText().toString().trim());
        if (times.isEmpty()) {
            Toast.makeText(this, "请填写时段", Toast.LENGTH_SHORT).show();
            return;
        }
        PlanRequest request = new PlanRequest(
                medicationId,
                frequency,
                times,
                emptyToNull(inputDose.getText().toString().trim()),
                switchWithMeal.isChecked(),
                emptyToNull(inputStart.getText().toString().trim()),
                emptyToNull(inputEnd.getText().toString().trim()),
                TimeZone.getDefault().getID(),
                true
        );
        RetrofitClient.getApiService().createPlan(request)
                .enqueue(new Callback<ApiResponse<Plan>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Plan>> call, Response<ApiResponse<Plan>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Plan plan = response.body().getData();
                            ReminderScheduler.scheduleNext(PlanEditActivity.this, plan, medicationId);
                            Toast.makeText(PlanEditActivity.this, "已创建提醒计划", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(PlanEditActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Plan>> call, Throwable t) {
                        Toast.makeText(PlanEditActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<String> parseTimes(String input) {
        if (TextUtils.isEmpty(input)) return new ArrayList<>();
        String[] parts = input.split(",");
        List<String> times = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                times.add(trimmed);
            }
        }
        return times;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}

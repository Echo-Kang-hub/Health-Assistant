package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecognitionConfirmActivity extends AppCompatActivity {
    private int recognitionId;
    private EditText inputName;
    private EditText inputSpec;
    private EditText inputUsage;
    private EditText inputFrequency;
    private EditText inputDose;
    private EditText inputTimes;
    private EditText inputStart;
    private EditText inputEnd;
    private Switch switchWithMeal;
    private TextView textConfidence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition_confirm);

        recognitionId = getIntent().getIntExtra("recognition_id", -1);

        inputName = findViewById(R.id.input_name);
        inputSpec = findViewById(R.id.input_spec);
        inputUsage = findViewById(R.id.input_usage);
        inputFrequency = findViewById(R.id.input_frequency);
        inputDose = findViewById(R.id.input_dose);
        inputTimes = findViewById(R.id.input_times);
        inputStart = findViewById(R.id.input_start);
        inputEnd = findViewById(R.id.input_end);
        switchWithMeal = findViewById(R.id.switch_with_meal);
        textConfidence = findViewById(R.id.text_confidence);

        Button buttonReparse = findViewById(R.id.button_ai_reparse);
        Button buttonFill = findViewById(R.id.button_ai_fill);
        Button buttonConfirm = findViewById(R.id.button_confirm);

        buttonReparse.setOnClickListener(v -> reparseRecognition("请重点识别用法用量"));
        buttonFill.setOnClickListener(v -> reparseRecognition("请补全缺失字段"));
        buttonConfirm.setOnClickListener(v -> confirmRecognition());

        if (recognitionId > 0) {
            loadRecognition();
        }
    }

    private void loadRecognition() {
        RetrofitClient.getApiService().getRecognition(recognitionId)
                .enqueue(new Callback<ApiResponse<Recognition>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Recognition>> call, Response<ApiResponse<Recognition>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Recognition rec = response.body().getData();
                            applyExtracted(rec.getExtracted());
                            if (rec.getConfidence() != null) {
                                textConfidence.setText("置信度 " + String.format("%.2f", rec.getConfidence()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Recognition>> call, Throwable t) {
                        Toast.makeText(RecognitionConfirmActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reparseRecognition(String hint) {
        if (recognitionId <= 0) {
            Toast.makeText(this, "请先完成识别", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.getApiService().reparseRecognition(new RecognitionReparseRequest(recognitionId, hint))
                .enqueue(new Callback<ApiResponse<RecognitionReparseResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RecognitionReparseResponse>> call, Response<ApiResponse<RecognitionReparseResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            RecognitionReparseResponse data = response.body().getData();
                            applyExtracted(data.getExtracted());
                            textConfidence.setText("置信度 " + String.format("%.2f", data.getConfidence()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RecognitionReparseResponse>> call, Throwable t) {
                        Toast.makeText(RecognitionConfirmActivity.this, "解析失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void applyExtracted(RecognitionExtracted extracted) {
        if (extracted == null) return;
        inputName.setText(nullToEmpty(extracted.getName()));
        inputSpec.setText(nullToEmpty(extracted.getSpec()));
        inputUsage.setText(nullToEmpty(extracted.getUsageText()));
        inputFrequency.setText(extracted.getFrequencyPerDay() != null ? String.valueOf(extracted.getFrequencyPerDay()) : "");
        inputDose.setText(nullToEmpty(extracted.getDose()));
        if (extracted.getTimes() != null && !extracted.getTimes().isEmpty()) {
            inputTimes.setText(TextUtils.join(",", extracted.getTimes()));
        }
        if (extracted.getWithMeal() != null) {
            switchWithMeal.setChecked(extracted.getWithMeal());
        }
    }

    private void confirmRecognition() {
        String name = inputName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "药品名称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        int frequency = parseInt(inputFrequency.getText().toString().trim(), 1);
        List<String> times = parseTimes(inputTimes.getText().toString().trim());
        if (times.isEmpty()) {
            times.add("08:00");
        }

        RecognitionConfirmRequest request = new RecognitionConfirmRequest(
                name,
                emptyToNull(inputSpec.getText().toString().trim()),
                emptyToNull(inputUsage.getText().toString().trim()),
                frequency,
                emptyToNull(inputDose.getText().toString().trim()),
                switchWithMeal.isChecked(),
                emptyToNull(inputStart.getText().toString().trim()),
                emptyToNull(inputEnd.getText().toString().trim()),
                times
        );

        if (recognitionId <= 0) {
            MedicationRequest medRequest = new MedicationRequest(
                    name,
                    emptyToNull(inputSpec.getText().toString().trim()),
                    emptyToNull(inputUsage.getText().toString().trim()),
                    frequency,
                    emptyToNull(inputDose.getText().toString().trim()),
                    switchWithMeal.isChecked(),
                    null,
                    emptyToNull(inputStart.getText().toString().trim()),
                    emptyToNull(inputEnd.getText().toString().trim())
            );
            RetrofitClient.getApiService().createMedication(medRequest)
                    .enqueue(new Callback<ApiResponse<Medication>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Medication>> call, Response<ApiResponse<Medication>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                Medication med = response.body().getData();
                                Intent intent = new Intent(RecognitionConfirmActivity.this, PlanEditActivity.class);
                                intent.putExtra("medication_id", med.getId());
                                intent.putExtra("plan_frequency", frequency);
                                intent.putExtra("plan_times", TextUtils.join(",", times));
                                intent.putExtra("plan_dose", inputDose.getText().toString().trim());
                                intent.putExtra("plan_with_meal", switchWithMeal.isChecked());
                                intent.putExtra("plan_start", inputStart.getText().toString().trim());
                                intent.putExtra("plan_end", inputEnd.getText().toString().trim());
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RecognitionConfirmActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Medication>> call, Throwable t) {
                            Toast.makeText(RecognitionConfirmActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                        }
                    });
            return;
        }

        RetrofitClient.getApiService().confirmRecognition(recognitionId, request)
                .enqueue(new Callback<ApiResponse<RecognitionConfirmResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RecognitionConfirmResponse>> call, Response<ApiResponse<RecognitionConfirmResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            RecognitionConfirmResponse data = response.body().getData();
                            Intent intent = new Intent(RecognitionConfirmActivity.this, PlanEditActivity.class);
                            intent.putExtra("medication_id", data.getMedicationId());
                            intent.putExtra("plan_frequency", data.getPlanDraft().getFrequencyPerDay());
                            intent.putExtra("plan_times", TextUtils.join(",", data.getPlanDraft().getTimes()));
                            intent.putExtra("plan_dose", data.getPlanDraft().getDose());
                            intent.putExtra("plan_with_meal", data.getPlanDraft().isWithMeal());
                            intent.putExtra("plan_start", data.getPlanDraft().getStartDate());
                            intent.putExtra("plan_end", data.getPlanDraft().getEndDate());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RecognitionConfirmActivity.this, "确认失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RecognitionConfirmResponse>> call, Throwable t) {
                        Toast.makeText(RecognitionConfirmActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}

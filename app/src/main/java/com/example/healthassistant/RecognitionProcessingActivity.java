package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecognitionProcessingActivity extends AppCompatActivity {
    private int recognitionId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition_processing);
        progressBar = findViewById(R.id.progress_recognition);
        Button backgroundButton = findViewById(R.id.button_background);

        recognitionId = getIntent().getIntExtra("recognition_id", -1);
        if (recognitionId <= 0) {
            finish();
            return;
        }

        backgroundButton.setOnClickListener(v -> finish());
        pollRecognition();
    }

    private void pollRecognition() {
        RetrofitClient.getApiService().getRecognition(recognitionId)
                .enqueue(new Callback<ApiResponse<Recognition>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Recognition>> call, Response<ApiResponse<Recognition>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Recognition rec = response.body().getData();
                            if ("SUCCEEDED".equals(rec.getStatus())) {
                                Intent intent = new Intent(RecognitionProcessingActivity.this, RecognitionConfirmActivity.class);
                                intent.putExtra("recognition_id", recognitionId);
                                startActivity(intent);
                                finish();
                            } else if ("FAILED".equals(rec.getStatus())) {
                                Toast.makeText(RecognitionProcessingActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                progressBar.setProgress(Math.min(progressBar.getProgress() + 10, 90));
                                scheduleNextPoll();
                            }
                        } else {
                            scheduleNextPoll();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Recognition>> call, Throwable t) {
                        scheduleNextPoll();
                    }
                });
    }

    private void scheduleNextPoll() {
        View view = getWindow().getDecorView();
        view.postDelayed(this::pollRecognition, 1500);
    }
}

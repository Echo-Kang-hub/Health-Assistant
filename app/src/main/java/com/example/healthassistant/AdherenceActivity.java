package com.example.healthassistant;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdherenceActivity extends AppCompatActivity {
    private TextView textSummary;
    private TextView textTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adherence);
        textSummary = findViewById(R.id.text_adherence_summary);
        textTips = findViewById(R.id.text_adherence_tips);
        Button buttonAi = findViewById(R.id.button_adherence_ai);

        buttonAi.setOnClickListener(v -> fetchTips());
    }

    private void fetchTips() {
        Calendar cal = Calendar.getInstance();
        String to = formatDate(cal);
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String from = formatDate(cal);

        RetrofitClient.getApiService().adherenceTips(new AdherenceTipsRequest(from, to))
                .enqueue(new Callback<ApiResponse<AdherenceTipsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AdherenceTipsResponse>> call, Response<ApiResponse<AdherenceTipsResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            AdherenceTipsResponse data = response.body().getData();
                            textTips.setText(joinLines(data.getTips(), data.getInsights()));
                        } else {
                            Toast.makeText(AdherenceActivity.this, "生成失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AdherenceTipsResponse>> call, Throwable t) {
                        Toast.makeText(AdherenceActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(cal.getTime());
    }

    private String joinLines(java.util.List<String> tips, java.util.List<String> insights) {
        StringBuilder builder = new StringBuilder();
        if (insights != null) {
            for (String line : insights) {
                builder.append("洞察：").append(line).append("\n");
            }
        }
        if (tips != null) {
            for (String line : tips) {
                builder.append("建议：").append(line).append("\n");
            }
        }
        return builder.toString().trim();
    }
}

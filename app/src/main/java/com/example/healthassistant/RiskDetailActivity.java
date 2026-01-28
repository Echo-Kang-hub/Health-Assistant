package com.example.healthassistant;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiskDetailActivity extends AppCompatActivity {
    private int riskId;
    private TextView textSummary;
    private TextView textExplain;
    private TextView textDisclaimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_risk_detail);

        riskId = getIntent().getIntExtra("risk_id", -1);
        textSummary = findViewById(R.id.text_risk_detail_summary);
        textExplain = findViewById(R.id.text_risk_explain);
        textDisclaimer = findViewById(R.id.text_risk_disclaimer);
        Button buttonExplain = findViewById(R.id.button_risk_explain);

        buttonExplain.setOnClickListener(v -> fetchExplain());

        loadRisk();
    }

    private void loadRisk() {
        RetrofitClient.getApiService().getRisk(riskId)
                .enqueue(new Callback<ApiResponse<Risk>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Risk>> call, Response<ApiResponse<Risk>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Risk risk = response.body().getData();
                            textSummary.setText(risk.getTitle() + "\n" + risk.getSummary());
                            textDisclaimer.setText(risk.getDisclaimer());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Risk>> call, Throwable t) {
                        Toast.makeText(RiskDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchExplain() {
        String tone = new SessionManager(this).getAiTone();
        RetrofitClient.getApiService().riskExplain(new RiskExplainRequest(riskId, tone))
                .enqueue(new Callback<ApiResponse<RiskExplainResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RiskExplainResponse>> call, Response<ApiResponse<RiskExplainResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            RiskExplainResponse data = response.body().getData();
                            textExplain.setText(data.getExplanation() + "\n" + joinLines(data.getNextSteps()));
                            textDisclaimer.setText(data.getDisclaimer());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RiskExplainResponse>> call, Throwable t) {
                        Toast.makeText(RiskDetailActivity.this, "生成失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String joinLines(java.util.List<String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append("- ").append(line).append("\n");
        }
        return builder.toString().trim();
    }
}

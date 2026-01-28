package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private TextView textNextDose;
    private TextView textRemaining;
    private MaterialCardView cardRisk;
    private TextView textRiskTitle;
    private TextView textRiskSummary;
    private TextView textSleep;
    private TextView textHeart;
    private TextView textGlucose;
    private Button buttonAiTips;
    private TextView textAiTips;
    private TextView textAiDisclaimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textNextDose = view.findViewById(R.id.text_next_dose_time);
        textRemaining = view.findViewById(R.id.text_remaining_count);
        cardRisk = view.findViewById(R.id.card_risk_banner);
        textRiskTitle = view.findViewById(R.id.text_risk_title);
        textRiskSummary = view.findViewById(R.id.text_risk_summary);
        textSleep = view.findViewById(R.id.text_sleep_value);
        textHeart = view.findViewById(R.id.text_heart_value);
        textGlucose = view.findViewById(R.id.text_glucose_value);
        buttonAiTips = view.findViewById(R.id.button_ai_tips);
        textAiTips = view.findViewById(R.id.text_ai_tips);
        textAiDisclaimer = view.findViewById(R.id.text_ai_disclaimer);

        view.findViewById(R.id.button_ai_assistant).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChatActivity.class));
        });
        view.findViewById(R.id.text_view_plan).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_meds);
            }
        });
        view.findViewById(R.id.text_risk_view).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RiskCenterActivity.class));
        });
        view.findViewById(R.id.button_quick_scan).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ScanActivity.class));
        });
        view.findViewById(R.id.button_quick_adherence).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(R.id.nav_reports);
            }
        });

        buttonAiTips.setOnClickListener(v -> fetchDailyTips());

        loadRisks();
        loadMetrics();
        loadNextDose();
    }

    private void loadNextDose() {
        RetrofitClient.getApiService().getPlans(true, 1, 1).enqueue(new Callback<ApiResponse<PagedResponse<Plan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<Plan>>> call, Response<ApiResponse<PagedResponse<Plan>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PagedResponse<Plan> data = response.body().getData();
                    if (data.getItems() != null && !data.getItems().isEmpty()) {
                        Plan plan = data.getItems().get(0);
                        RetrofitClient.getApiService().getNextOccurrences(plan.getId(), new OccurrenceRequest(1))
                                .enqueue(new Callback<ApiResponse<OccurrenceResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<OccurrenceResponse>> call, Response<ApiResponse<OccurrenceResponse>> response) {
                                        if (!isAdded()) return;
                                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null
                                                && response.body().getData().getItems() != null
                                                && !response.body().getData().getItems().isEmpty()) {
                                            String next = response.body().getData().getItems().get(0);
                                            textNextDose.setText(formatTime(next));
                                            textRemaining.setText("今日剩余 " + plan.getFrequencyPerDay() + " 次");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<OccurrenceResponse>> call, Throwable t) {
                                        if (!isAdded()) return;
                                        textNextDose.setText("--:--");
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<Plan>>> call, Throwable t) {
                if (!isAdded()) return;
                textNextDose.setText("--:--");
            }
        });
    }

    private void loadRisks() {
        RetrofitClient.getApiService().getRisks(true, 1, 1).enqueue(new Callback<ApiResponse<PagedResponse<Risk>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PagedResponse<Risk>>> call, Response<ApiResponse<PagedResponse<Risk>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    PagedResponse<Risk> data = response.body().getData();
                    if (data.getItems() != null && !data.getItems().isEmpty()) {
                        Risk risk = data.getItems().get(0);
                        cardRisk.setVisibility(View.VISIBLE);
                        textRiskTitle.setText(risk.getSeverity() + " · " + risk.getTitle());
                        textRiskSummary.setText(risk.getSummary());
                        cardRisk.setOnClickListener(v -> {
                            Intent intent = new Intent(requireContext(), RiskDetailActivity.class);
                            intent.putExtra("risk_id", risk.getId());
                            startActivity(intent);
                        });
                    } else {
                        cardRisk.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PagedResponse<Risk>>> call, Throwable t) {
                if (!isAdded()) return;
                cardRisk.setVisibility(View.GONE);
            }
        });
    }

    private void loadMetrics() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        RetrofitClient.getApiService().getMetrics("sleep", null, today, 1, 1)
                .enqueue(new MetricCallback(textSleep, "h"));
        RetrofitClient.getApiService().getMetrics("heart_rate", null, today, 1, 1)
                .enqueue(new MetricCallback(textHeart, "bpm"));
        RetrofitClient.getApiService().getMetrics("glucose", null, today, 1, 1)
                .enqueue(new MetricCallback(textGlucose, ""));
    }

    private void fetchDailyTips() {
        buttonAiTips.setEnabled(false);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());
        RetrofitClient.getApiService().dailyTips(new DailyTipsRequest(today))
                .enqueue(new Callback<ApiResponse<DailyTipsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DailyTipsResponse>> call, Response<ApiResponse<DailyTipsResponse>> response) {
                        buttonAiTips.setEnabled(true);
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            DailyTipsResponse tips = response.body().getData();
                            textAiTips.setText(joinLines(tips.getTips()));
                            textAiTips.setVisibility(View.VISIBLE);
                            textAiDisclaimer.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(requireContext(), "生成失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DailyTipsResponse>> call, Throwable t) {
                        buttonAiTips.setEnabled(true);
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String joinLines(java.util.List<String> lines) {
        if (lines == null || lines.isEmpty()) return "暂无建议";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            builder.append("· ").append(lines.get(i));
            if (i < lines.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private String formatTime(String iso) {
        if (iso == null || iso.length() < 16) return "--:--";
        return iso.substring(11, 16);
    }

    private static class MetricCallback implements Callback<ApiResponse<PagedResponse<Metric>>> {
        private final TextView textView;
        private final String suffix;

        MetricCallback(TextView textView, String suffix) {
            this.textView = textView;
            this.suffix = suffix;
        }

        @Override
        public void onResponse(Call<ApiResponse<PagedResponse<Metric>>> call, Response<ApiResponse<PagedResponse<Metric>>> response) {
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                PagedResponse<Metric> data = response.body().getData();
                if (data.getItems() != null && !data.getItems().isEmpty()) {
                    Metric metric = data.getItems().get(0);
                    textView.setText(metric.getValue() + (suffix.isEmpty() ? "" : " " + suffix));
                }
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<PagedResponse<Metric>>> call, Throwable t) {
        }
    }
}

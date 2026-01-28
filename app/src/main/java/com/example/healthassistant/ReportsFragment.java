package com.example.healthassistant;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {
    private TextView textSummary;
    private TextView textAiSummary;
    private Button buttonGenerate;
    private Button buttonAiSummary;
    private Integer latestReportId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textSummary = view.findViewById(R.id.text_report_summary);
        textAiSummary = view.findViewById(R.id.text_ai_summary);
        buttonGenerate = view.findViewById(R.id.button_generate_report);
        buttonAiSummary = view.findViewById(R.id.button_ai_summary);

        buttonGenerate.setOnClickListener(v -> generateWeeklyReport());
        buttonAiSummary.setOnClickListener(v -> fetchAiSummary());

        loadLatestReport();
    }

    private void generateWeeklyReport() {
        buttonGenerate.setEnabled(false);
        Calendar cal = Calendar.getInstance();
        String to = formatDate(cal);
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String from = formatDate(cal);
        ReportGenerateRequest request = new ReportGenerateRequest("WEEK", from, to);
        RetrofitClient.getApiService().generateReport(UUID.randomUUID().toString(), request)
                .enqueue(new Callback<ApiResponse<ReportJob>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportJob>> call, Response<ApiResponse<ReportJob>> response) {
                        buttonGenerate.setEnabled(true);
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            pollReportJob(response.body().getData().getId());
                        } else {
                            Toast.makeText(requireContext(), "生成失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportJob>> call, Throwable t) {
                        buttonGenerate.setEnabled(true);
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pollReportJob(int jobId) {
        RetrofitClient.getApiService().getReportJob(jobId)
                .enqueue(new Callback<ApiResponse<ReportJob>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportJob>> call, Response<ApiResponse<ReportJob>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            ReportJob job = response.body().getData();
                            if ("SUCCEEDED".equals(job.getStatus())) {
                                latestReportId = job.getReportId();
                                loadLatestReport();
                            } else if ("FAILED".equals(job.getStatus())) {
                                Toast.makeText(requireContext(), "生成失败", Toast.LENGTH_SHORT).show();
                            } else {
                                textSummary.setText("生成中..." + job.getProgress() + "%");
                                viewPostDelayed(() -> pollReportJob(jobId), 1500);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportJob>> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadLatestReport() {
        RetrofitClient.getApiService().getReports("WEEK", null, null, 1, 1)
                .enqueue(new Callback<ApiResponse<PagedResponse<Report>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PagedResponse<Report>>> call, Response<ApiResponse<PagedResponse<Report>>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null
                                && response.body().getData().getItems() != null
                                && !response.body().getData().getItems().isEmpty()) {
                            Report report = response.body().getData().getItems().get(0);
                            latestReportId = report.getId();
                            if (report.getContent() != null) {
                                textSummary.setText(report.getContent().getSummary());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PagedResponse<Report>>> call, Throwable t) {
                        if (!isAdded()) return;
                        textSummary.setText("加载失败");
                    }
                });
    }

    private void fetchAiSummary() {
        if (latestReportId == null) {
            Toast.makeText(requireContext(), "请先生成报告", Toast.LENGTH_SHORT).show();
            return;
        }
        buttonAiSummary.setEnabled(false);
        String tone = new SessionManager(requireContext()).getAiTone();
        RetrofitClient.getApiService().reportSummary(new ReportSummaryRequest(latestReportId, tone))
                .enqueue(new Callback<ApiResponse<ReportSummaryResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ReportSummaryResponse>> call, Response<ApiResponse<ReportSummaryResponse>> response) {
                        buttonAiSummary.setEnabled(true);
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            ReportSummaryResponse data = response.body().getData();
                            textAiSummary.setText(joinLines(data.getHighlights(), data.getActions()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ReportSummaryResponse>> call, Throwable t) {
                        buttonAiSummary.setEnabled(true);
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "生成失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void viewPostDelayed(Runnable runnable, long delayMs) {
        View view = getView();
        if (view != null) {
            view.postDelayed(runnable, delayMs);
        }
    }

    private String formatDate(Calendar cal) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(cal.getTime());
    }

    private String joinLines(java.util.List<String> highlights, java.util.List<String> actions) {
        StringBuilder builder = new StringBuilder();
        if (highlights != null) {
            for (String line : highlights) {
                builder.append("· ").append(line).append("\n");
            }
        }
        if (actions != null) {
            for (String line : actions) {
                builder.append("建议：").append(line).append("\n");
            }
        }
        return builder.length() > 0 ? builder.toString().trim() : "暂无总结";
    }
}

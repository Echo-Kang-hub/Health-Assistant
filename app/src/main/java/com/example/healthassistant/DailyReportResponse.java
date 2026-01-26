package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 每日健康/用药报告响应 (适配 v3.0 方案 A)
 */
public class DailyReportResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("report_date")
    private String reportDate;

    @SerializedName("summary")
    private String summary; // 对应原先的内容字段

    @SerializedName("recommendations")
    private List<Recommendation> recommendations;

    @SerializedName("health_tips")
    private List<String> healthTips;

    @SerializedName("error")
    private String error;

    // Getters
    public boolean isSuccess() { return success; }
    public String getReportDate() { return reportDate; }
    public String getSummary() { return summary; }
    public List<Recommendation> getRecommendations() { return recommendations; }
    public List<String> getHealthTips() { return healthTips; }
    public String getError() { return error; }

    /**
     * 内部类：健康建议
     */
    public static class Recommendation {
        @SerializedName("title")
        private String title;
        @SerializedName("content")
        private String content;
        @SerializedName("priority")
        private String priority; // HIGH / MEDIUM / LOW

        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getPriority() { return priority; }
    }
}
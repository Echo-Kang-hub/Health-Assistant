package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

/**
 * 每日健康/用药报告响应
 */
public class DailyReportResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("content")
    private String content; // 报告正文

    @SerializedName("report_date")
    private String reportDate; // 报告日期

    @SerializedName("error")
    private String error;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public String getContent() { return content; }
    public String getReportDate() { return reportDate; }
    public String getError() { return error; }
}
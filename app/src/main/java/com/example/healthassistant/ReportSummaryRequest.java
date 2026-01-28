package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class ReportSummaryRequest {
    @SerializedName("reportId")
    private int reportId;
    @SerializedName("tone")
    private String tone;

    public ReportSummaryRequest(int reportId, String tone) {
        this.reportId = reportId;
        this.tone = tone;
    }
}

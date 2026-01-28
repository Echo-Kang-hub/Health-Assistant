package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class ReportJob {
    @SerializedName("id")
    private int id;
    @SerializedName("status")
    private String status;
    @SerializedName("progress")
    private int progress;
    @SerializedName("reportId")
    private Integer reportId;
    @SerializedName("error")
    private String error;

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public Integer getReportId() {
        return reportId;
    }

    public String getError() {
        return error;
    }
}

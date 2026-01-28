package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class ReportGenerateRequest {
    @SerializedName("range")
    private String range;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;

    public ReportGenerateRequest(String range, String from, String to) {
        this.range = range;
        this.from = from;
        this.to = to;
    }
}

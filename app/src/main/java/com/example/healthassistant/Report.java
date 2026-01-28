package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class Report {
    @SerializedName("id")
    private int id;
    @SerializedName("range")
    private String range;
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;
    @SerializedName("content")
    private ReportContent content;
    @SerializedName("generatedAt")
    private String generatedAt;

    public int getId() {
        return id;
    }

    public String getRange() {
        return range;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public ReportContent getContent() {
        return content;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }
}

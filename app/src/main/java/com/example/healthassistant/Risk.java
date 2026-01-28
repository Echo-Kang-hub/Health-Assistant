package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class Risk {
    @SerializedName("id")
    private int id;
    @SerializedName("type")
    private String type;
    @SerializedName("severity")
    private String severity;
    @SerializedName("title")
    private String title;
    @SerializedName("summary")
    private String summary;
    @SerializedName("evidence")
    private Object evidence;
    @SerializedName("action")
    private Object action;
    @SerializedName("active")
    private boolean active;
    @SerializedName("createdAt")
    private String createdAt;
    @SerializedName("disclaimer")
    private String disclaimer;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public Object getEvidence() {
        return evidence;
    }

    public Object getAction() {
        return action;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

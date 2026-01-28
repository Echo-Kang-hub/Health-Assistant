package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class DoseLog {
    @SerializedName("id")
    private int id;
    @SerializedName("planId")
    private Integer planId;
    @SerializedName("medicationId")
    private int medicationId;
    @SerializedName("scheduledAt")
    private String scheduledAt;
    @SerializedName("takenAt")
    private String takenAt;
    @SerializedName("status")
    private String status;
    @SerializedName("reason")
    private String reason;

    public int getId() {
        return id;
    }

    public Integer getPlanId() {
        return planId;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public String getTakenAt() {
        return takenAt;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}

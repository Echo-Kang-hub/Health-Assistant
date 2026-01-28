package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class DoseLogRequest {
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

    public DoseLogRequest(Integer planId, int medicationId, String scheduledAt, String takenAt,
                          String status, String reason) {
        this.planId = planId;
        this.medicationId = medicationId;
        this.scheduledAt = scheduledAt;
        this.takenAt = takenAt;
        this.status = status;
        this.reason = reason;
    }
}

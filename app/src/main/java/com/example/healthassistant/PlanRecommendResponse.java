package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlanRecommendResponse {
    @SerializedName("times")
    private List<String> times;
    @SerializedName("confidence")
    private float confidence;
    @SerializedName("reason")
    private String reason;

    public List<String> getTimes() {
        return times;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }
}

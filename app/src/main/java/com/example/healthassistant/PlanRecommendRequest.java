package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class PlanRecommendRequest {
    @SerializedName("medicationName")
    private String medicationName;
    @SerializedName("usageText")
    private String usageText;
    @SerializedName("frequencyPerDay")
    private int frequencyPerDay;
    @SerializedName("timezone")
    private String timezone;

    public PlanRecommendRequest(String medicationName, String usageText, int frequencyPerDay, String timezone) {
        this.medicationName = medicationName;
        this.usageText = usageText;
        this.frequencyPerDay = frequencyPerDay;
        this.timezone = timezone;
    }
}

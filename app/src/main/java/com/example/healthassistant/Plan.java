package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Plan {
    @SerializedName("id")
    private int id;
    @SerializedName("medicationId")
    private int medicationId;
    @SerializedName("frequencyPerDay")
    private int frequencyPerDay;
    @SerializedName("times")
    private List<String> times;
    @SerializedName("dose")
    private String dose;
    @SerializedName("withMeal")
    private Boolean withMeal;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("timezone")
    private String timezone;
    @SerializedName("active")
    private boolean active;

    public int getId() {
        return id;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public List<String> getTimes() {
        return times;
    }

    public String getDose() {
        return dose;
    }

    public Boolean getWithMeal() {
        return withMeal;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean isActive() {
        return active;
    }
}

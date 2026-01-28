package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlanRequest {
    @SerializedName("medicationId")
    private int medicationId;
    @SerializedName("frequencyPerDay")
    private int frequencyPerDay;
    @SerializedName("times")
    private List<String> times;
    @SerializedName("dose")
    private String dose;
    @SerializedName("withMeal")
    private boolean withMeal;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("timezone")
    private String timezone;
    @SerializedName("active")
    private boolean active;

    public PlanRequest(int medicationId, int frequencyPerDay, List<String> times, String dose,
                       boolean withMeal, String startDate, String endDate, String timezone,
                       boolean active) {
        this.medicationId = medicationId;
        this.frequencyPerDay = frequencyPerDay;
        this.times = times;
        this.dose = dose;
        this.withMeal = withMeal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timezone = timezone;
        this.active = active;
    }
}

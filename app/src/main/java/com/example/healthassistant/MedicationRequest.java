package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class MedicationRequest {
    @SerializedName("name")
    private String name;
    @SerializedName("spec")
    private String spec;
    @SerializedName("usageText")
    private String usageText;
    @SerializedName("frequencyPerDay")
    private Integer frequencyPerDay;
    @SerializedName("dose")
    private String dose;
    @SerializedName("withMeal")
    private Boolean withMeal;
    @SerializedName("remainCount")
    private Integer remainCount;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;

    public MedicationRequest(String name, String spec, String usageText, Integer frequencyPerDay,
                             String dose, Boolean withMeal, Integer remainCount,
                             String startDate, String endDate) {
        this.name = name;
        this.spec = spec;
        this.usageText = usageText;
        this.frequencyPerDay = frequencyPerDay;
        this.dose = dose;
        this.withMeal = withMeal;
        this.remainCount = remainCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

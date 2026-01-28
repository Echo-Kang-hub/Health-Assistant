package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class Medication {
    @SerializedName("id")
    private int id;
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
    @SerializedName("createdAt")
    private String createdAt;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpec() {
        return spec;
    }

    public String getUsageText() {
        return usageText;
    }

    public Integer getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public String getDose() {
        return dose;
    }

    public Boolean getWithMeal() {
        return withMeal;
    }

    public Integer getRemainCount() {
        return remainCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

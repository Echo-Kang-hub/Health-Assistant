package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecognitionExtracted {
    @SerializedName("name")
    private String name;
    @SerializedName("spec")
    private String spec;
    @SerializedName("usage_text")
    private String usageText;
    @SerializedName("frequency_per_day")
    private Integer frequencyPerDay;
    @SerializedName("dose")
    private String dose;
    @SerializedName("with_meal")
    private Boolean withMeal;
    @SerializedName("times")
    private List<String> times;

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

    public List<String> getTimes() {
        return times;
    }
}

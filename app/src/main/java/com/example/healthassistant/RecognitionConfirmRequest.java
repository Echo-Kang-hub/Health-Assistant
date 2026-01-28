package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecognitionConfirmRequest {
    @SerializedName("name")
    private String name;
    @SerializedName("spec")
    private String spec;
    @SerializedName("usageText")
    private String usageText;
    @SerializedName("frequencyPerDay")
    private int frequencyPerDay;
    @SerializedName("dose")
    private String dose;
    @SerializedName("withMeal")
    private boolean withMeal;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("times")
    private List<String> times;

    public RecognitionConfirmRequest(String name, String spec, String usageText, int frequencyPerDay,
                                     String dose, boolean withMeal, String startDate, String endDate,
                                     List<String> times) {
        this.name = name;
        this.spec = spec;
        this.usageText = usageText;
        this.frequencyPerDay = frequencyPerDay;
        this.dose = dose;
        this.withMeal = withMeal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.times = times;
    }
}

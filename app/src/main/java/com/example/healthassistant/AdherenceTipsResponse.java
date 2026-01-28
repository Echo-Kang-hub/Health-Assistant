package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdherenceTipsResponse {
    @SerializedName("tips")
    private List<String> tips;
    @SerializedName("insights")
    private List<String> insights;
    @SerializedName("disclaimer")
    private String disclaimer;

    public List<String> getTips() {
        return tips;
    }

    public List<String> getInsights() {
        return insights;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

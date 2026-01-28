package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DailyTipsResponse {
    @SerializedName("tips")
    private List<String> tips;
    @SerializedName("disclaimer")
    private String disclaimer;

    public List<String> getTips() {
        return tips;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

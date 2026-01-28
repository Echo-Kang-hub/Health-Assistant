package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ReportContent {
    @SerializedName("summary")
    private String summary;
    @SerializedName("insights")
    private List<String> insights;
    @SerializedName("trends")
    private Map<String, Object> trends;
    @SerializedName("actions")
    private List<String> actions;

    public String getSummary() {
        return summary;
    }

    public List<String> getInsights() {
        return insights;
    }

    public Map<String, Object> getTrends() {
        return trends;
    }

    public List<String> getActions() {
        return actions;
    }
}

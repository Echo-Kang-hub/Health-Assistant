package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReportSummaryResponse {
    @SerializedName("highlights")
    private List<String> highlights;
    @SerializedName("actions")
    private List<String> actions;
    @SerializedName("disclaimer")
    private String disclaimer;

    public List<String> getHighlights() {
        return highlights;
    }

    public List<String> getActions() {
        return actions;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

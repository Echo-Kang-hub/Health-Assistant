package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RiskExplainResponse {
    @SerializedName("explanation")
    private String explanation;
    @SerializedName("nextSteps")
    private List<String> nextSteps;
    @SerializedName("disclaimer")
    private String disclaimer;

    public String getExplanation() {
        return explanation;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}

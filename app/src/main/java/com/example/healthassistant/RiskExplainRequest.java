package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class RiskExplainRequest {
    @SerializedName("riskId")
    private int riskId;
    @SerializedName("tone")
    private String tone;

    public RiskExplainRequest(int riskId, String tone) {
        this.riskId = riskId;
        this.tone = tone;
    }
}

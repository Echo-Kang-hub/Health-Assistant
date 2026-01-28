package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class RecognitionConfirmResponse {
    @SerializedName("medicationId")
    private int medicationId;
    @SerializedName("planDraft")
    private PlanDraft planDraft;

    public int getMedicationId() {
        return medicationId;
    }

    public PlanDraft getPlanDraft() {
        return planDraft;
    }
}

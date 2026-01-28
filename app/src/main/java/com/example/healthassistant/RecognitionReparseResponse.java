package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class RecognitionReparseResponse {
    @SerializedName("extracted")
    private RecognitionExtracted extracted;
    @SerializedName("confidence")
    private float confidence;
    @SerializedName("rawUsageText")
    private String rawUsageText;

    public RecognitionExtracted getExtracted() {
        return extracted;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getRawUsageText() {
        return rawUsageText;
    }
}

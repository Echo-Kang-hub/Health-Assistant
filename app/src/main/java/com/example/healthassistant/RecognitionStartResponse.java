package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class RecognitionStartResponse {
    @SerializedName("recognitionId")
    private int recognitionId;
    @SerializedName("status")
    private String status;

    public int getRecognitionId() {
        return recognitionId;
    }

    public String getStatus() {
        return status;
    }
}

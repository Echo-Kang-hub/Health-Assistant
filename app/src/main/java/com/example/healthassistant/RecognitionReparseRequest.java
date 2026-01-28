package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class RecognitionReparseRequest {
    @SerializedName("recognitionId")
    private int recognitionId;
    @SerializedName("hint")
    private String hint;

    public RecognitionReparseRequest(int recognitionId, String hint) {
        this.recognitionId = recognitionId;
        this.hint = hint;
    }
}

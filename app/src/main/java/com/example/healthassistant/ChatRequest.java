package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ChatRequest {
    @SerializedName("message")
    private String message;
    @SerializedName("tone")
    private String tone;
    @SerializedName("context")
    private Map<String, Object> context;

    public ChatRequest(String message, String tone, Map<String, Object> context) {
        this.message = message;
        this.tone = tone;
        this.context = context;
    }
}

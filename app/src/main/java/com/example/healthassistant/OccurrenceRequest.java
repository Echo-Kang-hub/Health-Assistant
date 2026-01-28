package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class OccurrenceRequest {
    @SerializedName("count")
    private int count;

    public OccurrenceRequest(int count) {
        this.count = count;
    }
}

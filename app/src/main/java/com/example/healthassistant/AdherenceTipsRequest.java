package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class AdherenceTipsRequest {
    @SerializedName("from")
    private String from;
    @SerializedName("to")
    private String to;

    public AdherenceTipsRequest(String from, String to) {
        this.from = from;
        this.to = to;
    }
}

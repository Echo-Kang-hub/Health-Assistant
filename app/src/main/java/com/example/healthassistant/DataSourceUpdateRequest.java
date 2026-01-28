package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class DataSourceUpdateRequest {
    @SerializedName("status")
    private String status;
    @SerializedName("provider")
    private String provider;

    public DataSourceUpdateRequest(String status, String provider) {
        this.status = status;
        this.provider = provider;
    }
}

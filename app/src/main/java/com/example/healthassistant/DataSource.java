package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class DataSource {
    @SerializedName("id")
    private int id;
    @SerializedName("type")
    private String type;
    @SerializedName("status")
    private String status;
    @SerializedName("provider")
    private String provider;
    @SerializedName("lastSyncAt")
    private String lastSyncAt;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public String getLastSyncAt() {
        return lastSyncAt;
    }
}

package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class DailyTipsRequest {
    @SerializedName("date")
    private String date;

    public DailyTipsRequest(String date) {
        this.date = date;
    }
}

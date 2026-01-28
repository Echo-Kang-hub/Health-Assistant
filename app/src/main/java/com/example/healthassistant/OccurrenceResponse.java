package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OccurrenceResponse {
    @SerializedName("items")
    private List<String> items;

    public List<String> getItems() {
        return items;
    }
}

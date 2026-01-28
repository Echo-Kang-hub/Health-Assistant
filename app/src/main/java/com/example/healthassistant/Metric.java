package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class Metric {
    @SerializedName("id")
    private int id;
    @SerializedName("type")
    private String type;
    @SerializedName("ts")
    private String ts;
    @SerializedName("value")
    private double value;
    @SerializedName("unit")
    private String unit;
    @SerializedName("source")
    private String source;

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTs() {
        return ts;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getSource() {
        return source;
    }
}

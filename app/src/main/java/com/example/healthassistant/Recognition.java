package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class Recognition {
    @SerializedName("id")
    private int id;
    @SerializedName("status")
    private String status;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("barcodeText")
    private String barcodeText;
    @SerializedName("extracted")
    private RecognitionExtracted extracted;
    @SerializedName("confidence")
    private Float confidence;
    @SerializedName("rawUsageText")
    private String rawUsageText;
    @SerializedName("error")
    private String error;
    @SerializedName("createdAt")
    private String createdAt;

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBarcodeText() {
        return barcodeText;
    }

    public RecognitionExtracted getExtracted() {
        return extracted;
    }

    public Float getConfidence() {
        return confidence;
    }

    public String getRawUsageText() {
        return rawUsageText;
    }

    public String getError() {
        return error;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class ApiError {
    @SerializedName("code")
    private String code;
    @SerializedName("message")
    private String message;
    @SerializedName("details")
    private Object details;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }
}

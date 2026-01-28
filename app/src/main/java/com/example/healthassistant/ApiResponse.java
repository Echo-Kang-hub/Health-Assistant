package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("requestId")
    private String requestId;
    @SerializedName("data")
    private T data;
    @SerializedName("error")
    private ApiError error;

    public String getRequestId() {
        return requestId;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}

package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("password")
    private String password;

    public AuthRequest(String email, String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
    }
}

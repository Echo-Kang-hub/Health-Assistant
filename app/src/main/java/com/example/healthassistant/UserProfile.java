package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("timezone")
    private String timezone;
    @SerializedName("name")
    private String name;
    @SerializedName("age")
    private Integer age;

    public UserProfile(String timezone, String name, Integer age) {
        this.timezone = timezone;
        this.name = name;
        this.age = age;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}

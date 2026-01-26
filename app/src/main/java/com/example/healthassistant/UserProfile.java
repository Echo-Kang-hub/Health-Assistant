package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 用户健康档案，用于个性化风险分析。
 * 后端指示: 此数据需要序列化为 JSON 字符串，通过 user_profile 字段发送。
 */
public class UserProfile {
    @SerializedName("weight_kg")
    private Double weightKg; // 体重 (kg)

    @SerializedName("height_cm")
    private Integer heightCm; // 身高 (cm)

    @SerializedName("allergies")
    private List<String> allergies; // 已知过敏药物列表

    @SerializedName("kidney_function")
    private String kidneyFunction; // 肾功能状态

    @SerializedName("pregnancy_status")
    private String pregnancyStatus; // 孕期/哺乳期/儿童状态

    // 构造函数
    public UserProfile(Double weightKg, Integer heightCm, List<String> allergies, String kidneyFunction, String pregnancyStatus) {
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.allergies = allergies;
        this.kidneyFunction = kidneyFunction;
        this.pregnancyStatus = pregnancyStatus;
    }

    // Getters (简化，仅提供核心方法)
    public Double getWeightKg() { return weightKg; }
    public Integer getHeightCm() { return heightCm; }
    public List<String> getAllergies() { return allergies; }
    public String getKidneyFunction() { return kidneyFunction; }
    public String getPregnancyStatus() { return pregnancyStatus; }
}
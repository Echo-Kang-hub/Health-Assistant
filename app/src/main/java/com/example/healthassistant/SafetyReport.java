package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * AI 对用药计划的安全性分析报告
 * 后端指示: processPrescription 响应体中 safety_report 的顶层结构。
 */
public class SafetyReport {
    @SerializedName("is_safe")
    private boolean isSafe; // 总体安全性判断

    @SerializedName("overall_message")
    private String overallMessage; // 总体建议或警告

    @SerializedName("interactions")
    private List<Interaction> interactions; // 药物相互作用列表

    @SerializedName("personalized_risks")
    private List<Risk> personalizedRisks; // 个性化风险列表 (过敏, 孕期, 肝肾功能等)

    @SerializedName("dose_anomalies")
    private List<String> doseAnomalies; // 剂量异常药物列表

    @SerializedName("duplicates")
    private List<String> duplicates; // 重复用药药物列表

    // Getters
    public boolean isSafe() { return isSafe; }
    public String getOverallMessage() { return overallMessage; }
    public List<Interaction> getInteractions() { return interactions; }
    public List<Risk> getPersonalizedRisks() { return personalizedRisks; }
    public List<String> getDoseAnomalies() { return doseAnomalies; }
    public List<String> getDuplicates() { return duplicates; }

    // Setters (可选)
    public void setSafe(boolean safe) { isSafe = safe; }
    public void setOverallMessage(String overallMessage) { this.overallMessage = overallMessage; }
    public void setInteractions(List<Interaction> interactions) { this.interactions = interactions; }
    public void setPersonalizedRisks(List<Risk> personalizedRisks) { this.personalizedRisks = personalizedRisks; }
    public void setDoseAnomalies(List<String> doseAnomalies) { this.doseAnomalies = doseAnomalies; }
    public void setDuplicates(List<String> duplicates) { this.duplicates = duplicates; }
}
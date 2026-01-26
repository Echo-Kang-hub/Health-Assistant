package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

/**
 * 描述药物相互作用的风险
 * 后端指示: 需要在 processPrescription 响应体的 safety_report 字段中填充此对象列表。
 */
public class Interaction {
    @SerializedName("drug_a")
    private String drugA;

    @SerializedName("drug_b")
    private String drugB;

    @SerializedName("severity")
    private String severity; // 例如: LOW, MEDIUM, HIGH

    @SerializedName("warning")
    private String warning;

    // Getters
    public String getDrugA() { return drugA; }
    public String getDrugB() { return drugB; }
    public String getSeverity() { return severity; }
    public String getWarning() { return warning; }

    // Setters (可选，通常用于数据模型)
    public void setDrugA(String drugA) { this.drugA = drugA; }
    public void setDrugB(String drugB) { this.drugB = drugB; }
    public void setSeverity(String severity) { this.severity = severity; }
    public void setWarning(String warning) { this.warning = warning; }
}
package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;

/**
 * 描述基于用户档案的个性化风险（如过敏、孕期/肝肾功能限制）
 * 后端指示: 需要在 processPrescription 响应体的 safety_report 字段中填充此对象列表。
 */
public class Risk {
    @SerializedName("risk_type")
    private String riskType; // 例如: ALLERGY, PREGNANCY, LIVER_IMPAIRMENT

    @SerializedName("drug")
    private String drug; // 涉及风险的药物名称

    @SerializedName("warning")
    private String warning;

    // Getters
    public String getRiskType() { return riskType; }
    public String getDrug() { return drug; }
    public String getWarning() { return warning; }

    // Setters (可选)
    public void setRiskType(String riskType) { this.riskType = riskType; }
    public void setDrug(String drug) { this.drug = drug; }
    public void setWarning(String warning) { this.warning = warning; }
}
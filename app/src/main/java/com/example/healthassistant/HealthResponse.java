package com.example.healthassistant;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 后端返回的整体 AI 处理结果
 */
public class HealthResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("error")
    private String error;

    @SerializedName("diagnosis")
    private String diagnosis;

    @SerializedName("medication_plan")
    private List<MedicinePlan> medicationPlan;

    @SerializedName("notes")
    private String notes;

    // Getter and Setter for new fields
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    // Existing Getter and Setter
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public List<MedicinePlan> getMedicationPlan() { return medicationPlan; }
    public void setMedicationPlan(List<MedicinePlan> medicationPlan) { this.medicationPlan = medicationPlan; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
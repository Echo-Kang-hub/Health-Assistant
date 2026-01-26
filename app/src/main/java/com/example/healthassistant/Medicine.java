package com.example.healthassistant;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 数据库中的药品表格，已支持吃药提醒
 */
@Entity(tableName = "medicine_table")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id; 

    private String name;
    private String dosage;
    private String frequency;
    private long timestamp; 

    private boolean isReminderEnabled; // 是否开启提醒
    private String reminderTime;        // 提醒时间 (格式 "HH:mm")

    // 构造函数
    public Medicine(String name, String dosage, String frequency, long timestamp) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.timestamp = timestamp;
        this.isReminderEnabled = false;
        this.reminderTime = "";
    }

    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isReminderEnabled() { return isReminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { isReminderEnabled = reminderEnabled; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
}
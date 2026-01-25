package com.example.healthassistant;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 数据库中的药品表格
 */
@Entity(tableName = "medicine_table")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id; // 自动生成的唯一ID

    private String name;
    private String dosage;
    private String frequency;
    private long timestamp; // 记录时间

    // 构造函数
    public Medicine(String name, String dosage, String frequency, long timestamp) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.timestamp = timestamp;
    }

    // Getter 和 Setter (Room 必须)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public long getTimestamp() { return timestamp; }
}
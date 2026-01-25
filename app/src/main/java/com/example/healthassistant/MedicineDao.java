package com.example.healthassistant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MedicineDao {
    @Insert
    void insert(Medicine medicine);

    @Query("SELECT * FROM medicine_table ORDER BY timestamp DESC")
    List<Medicine> getAllMedicines();

    @Query("DELETE FROM medicine_table")
    void deleteAll();
}
package com.oop.db.oop;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlanDao {
    @Insert
    void addPlan(Plan plan);

    @Update
    void updatePlan(Plan plan);

    @Delete
    void deletePlan(Plan plan);

    @Query("SELECT * FROM plans WHERE id = :id")
    Plan getPlanById(int id);

    @Query("SELECT * FROM plans")
    List<Plan> getAllPlans();
}
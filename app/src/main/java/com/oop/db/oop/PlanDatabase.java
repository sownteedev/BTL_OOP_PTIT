package com.oop.db.oop;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Plan.class}, version = 3) // Incremented version number
public abstract class PlanDatabase extends RoomDatabase {
    public abstract PlanDao getPlanDao();
}
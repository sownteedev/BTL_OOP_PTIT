package com.oop.db.oop;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CashItem.class}, version = 4)
public abstract class CashFlowDatabase extends RoomDatabase {
    public abstract CashFlowDao getCashFlowDao();
}

package com.zamasaur.riscocontrol.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {

    @Insert
    void insert(LogEntry entry);

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getAllEntries();
}
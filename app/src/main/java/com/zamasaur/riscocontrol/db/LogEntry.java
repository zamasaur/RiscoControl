package com.zamasaur.riscocontrol.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "log_entries")
public class LogEntry {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String message;
    public long timestamp;

    public LogEntry(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
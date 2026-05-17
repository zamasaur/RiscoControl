package com.zamasaur.riscocontrol.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LogEntry.class}, version = 1, exportSchema = false)
public abstract class LogDatabase extends RoomDatabase {

    public abstract LogDao logDao();

    private static volatile LogDatabase INSTANCE;

    public static LogDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LogDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            LogDatabase.class,
                            "risco_log_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
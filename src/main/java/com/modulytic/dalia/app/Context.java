package com.modulytic.dalia.app;

import com.modulytic.dalia.app.database.include.Database;

public final class Context {
    private Context() {}

    private static Database database;

    public static void setDatabase(Database db) {
        database = db;
    }

    public static Database getDatabase() {
        return database;
    }
}

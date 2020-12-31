package com.modulytic.dalia.app;

import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.ws.WsdServer;

public final class Context {
    private Context() {}

    private static Database database;
    private static WsdServer wsdServer;

    public static void setDatabase(Database db) {
        database = db;
    }

    public static Database getDatabase() {
        return database;
    }

    public static void setWsdServer(WsdServer server) {
        wsdServer = server;
    }

    public static WsdServer getWsdServer() {
        return wsdServer;
    }
}

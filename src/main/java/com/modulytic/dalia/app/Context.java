package com.modulytic.dalia.app;

import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import com.modulytic.dalia.ws.WsdServer;

public final class Context {
    private Context() {}

    private static Database database;
    private static DaliaSmppSessionListener sessionListener;
    private static WsdServer wsdServer;

    public static void setDatabase(Database db) {
        database = db;
    }

    public static Database getDatabase() {
        return database;
    }

    public static void setSessionListener(DaliaSmppSessionListener listener) {
        sessionListener = listener;
    }

    public static DaliaSmppSessionListener getSessionListener() {
        return sessionListener;
    }

    public static void setWsdServer(WsdServer server) {
        wsdServer = server;
    }

    public static WsdServer getWsdServer() {
        return wsdServer;
    }
}

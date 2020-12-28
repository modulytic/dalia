package com.modulytic.dalia;

import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.smpp.DLRUpdateHandler;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import com.modulytic.dalia.ws.WsdServer;

public final class DaliaContext {
    private DaliaContext() {}

    private static DbManager database;
    private static DaliaSmppSessionListener sessionListener;
    private static DLRUpdateHandler updateHandler;
    private static WsdServer wsdServer;

    public static void setDatabase(DbManager db) {
        database = db;
    }

    public static DbManager getDatabase() {
        return database;
    }

    public static void setSessionListener(DaliaSmppSessionListener listener) {
        sessionListener = listener;
    }

    public static DaliaSmppSessionListener getSessionListener() {
        return sessionListener;
    }

    public static void setDLRUpdateHandler(DLRUpdateHandler handler) {
        updateHandler = handler;
    }

    public static DLRUpdateHandler getDLRUpdateHandler() {
        return updateHandler;
    }

    public static void setWsdServer(WsdServer server) {
        wsdServer = server;
    }

    public static WsdServer getWsdServer() {
        return wsdServer;
    }
}

package com.modulytic.dalia.ws;

import com.modulytic.dalia.app.Context;

public final class WsdThreadSpawner {
    private WsdThreadSpawner() {}

    public static void start(final WsdServer server) {
        Context.setWsdServer(server);
        Thread t = new Thread(server);
        t.start();
    }
}

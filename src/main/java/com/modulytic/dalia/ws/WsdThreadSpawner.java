package com.modulytic.dalia.ws;

public final class WsdThreadSpawner {
    private WsdThreadSpawner() {}

    public static void start(final WsdServer server) {
        Thread t = new Thread(server);
        t.start();
    }
}

package com.modulytic.dalia.ws;

public class WsdServerThread extends Thread {
    private final WsdServer server;
    public WsdServerThread(WsdServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        super.run();

        this.server.run();
    }
}

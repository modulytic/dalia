package com.modulytic.dalia.ws;

/**
 * {@link WsdServer#run()} is blocking, so we create a special thread to run our server on
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class WsdServerThread extends Thread {
    /**
     * The server to run
     */
    private final WsdServer server;

    /**
     * Create thread and load server into it
     * @param server    the server to run
     */
    public WsdServerThread(WsdServer server) {
        this.server = server;
    }

    /**
     * Start thread
     */
    @Override
    public void run() {
        super.run();

        this.server.run();
    }
}

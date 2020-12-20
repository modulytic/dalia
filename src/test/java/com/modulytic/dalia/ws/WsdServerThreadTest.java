package com.modulytic.dalia.ws;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class WsdServerThreadTest {

    @Test
    void runStartsWsdServer() throws InterruptedException {
        WsdServer server = mock(WsdServer.class);

        WsdServerThread thread = new WsdServerThread(server);
        thread.start();

        // prevent race condition
        Thread.sleep(300);

        verify(server).run();
    }
}
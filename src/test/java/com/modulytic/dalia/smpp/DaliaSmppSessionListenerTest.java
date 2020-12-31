package com.modulytic.dalia.smpp;

import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import net.gescobar.smppserver.SmppSession;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DaliaSmppSessionListenerTest {
    @Test
    void pendingSessionBridgesAreNotReturned() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");

        listener.created(session);

        assertNull(listener.getSessionBridge("sysid"));
    }

    @Test
    void boundSessionBridgesAreReturned() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(true);

        listener.created(session);
        listener.activateSession("sysid");

        assertNotNull(listener.getSessionBridge("sysid"));
    }

    @Test
    void sessionCannotBeFalselyMarkedBound() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(false);

        listener.created(session);
        listener.activateSession("sysid");

        assertNull(listener.getSessionBridge("sysid"));
    }

    @Test
    void destroyedSessionBridgesAreNotReturned() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(true);

        listener.created(session);
        listener.activateSession("sysid");

        assertNotNull(listener.getSessionBridge("sysid"));

        listener.destroyed(session);
        assertNull(listener.getSessionBridge("sysid"));
    }
}
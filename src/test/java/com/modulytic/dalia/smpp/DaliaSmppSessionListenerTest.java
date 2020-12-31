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

        assertNull(DaliaSmppSessionListener.getSessionBridge("sysid"));
    }

    @Test
    void boundSessionBridgesAreReturned() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(true);

        listener.created(session);
        DaliaSmppSessionListener.activate("sysid");

        assertNotNull(DaliaSmppSessionListener.getSessionBridge("sysid"));
    }

    @Test
    void sessionCannotBeFalselyMarkedBound() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(false);

        listener.created(session);
        DaliaSmppSessionListener.activate("sysid");

        assertNull(DaliaSmppSessionListener.getSessionBridge("sysid"));
    }

    @Test
    void destroyedSessionBridgesAreNotReturned() {
        DaliaSmppSessionListener listener = new DaliaSmppSessionListener();

        SmppSession session = mock(SmppSession.class);
        when(session.getSystemId()).thenReturn("sysid");
        when(session.isBound()).thenReturn(true);

        listener.created(session);
        DaliaSmppSessionListener.activate(session.getSystemId());

        assertNotNull(DaliaSmppSessionListener.getSessionBridge("sysid"));

        DaliaSmppSessionListener.deactivate(session.getSystemId());
        listener.destroyed(session);
        assertNull(DaliaSmppSessionListener.getSessionBridge("sysid"));
    }
}
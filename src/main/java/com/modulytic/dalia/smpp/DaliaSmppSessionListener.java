package com.modulytic.dalia.smpp;

import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.SmppSessionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Store active connections in a hashmap so we can access them quickly
public class DaliaSmppSessionListener implements SmppSessionListener {
    final private Map<String, SmppSession> boundSessions;
    final private List<SmppSession> pendingSessions;

    public DaliaSmppSessionListener() {
        this.boundSessions = new HashMap<>();
        this.pendingSessions = new ArrayList<>();
    }

    public SmppSession getSession(String systemId) {
        return this.boundSessions.get(systemId);
    }

    @Override
    public void created(SmppSession smppSession) {
        // created, but may never be bound
        this.pendingSessions.add(smppSession);
    }

    public void activateSession(String systemId) {
        for (SmppSession session : pendingSessions) {
            if (session.isBound() && session.getSystemId().equals(systemId)) {
                this.pendingSessions.remove(session);
                this.boundSessions.put(systemId, session);

                break;
            }
        }
    }

    @Override
    public void destroyed(SmppSession smppSession) {
        if (smppSession.isBound()) {
            this.boundSessions.remove(smppSession.getSystemId());
        }
        else {
            this.pendingSessions.remove(smppSession);
        }
    }
}

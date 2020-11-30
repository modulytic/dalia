package com.modulytic.dalia.smpp;

import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.SmppSessionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Store active connections in a hashmap so we can access them quickly
public class DaliaSmppSessionListener implements SmppSessionListener {
    final private Map<String, DaliaSessionBridge> bridges;

    final private List<SmppSession> pendingSessions;

    public DaliaSmppSessionListener() {
        this.bridges = new HashMap<>();

        this.pendingSessions = new ArrayList<>();
    }

    public DaliaSessionBridge getSessionBridge(String systemId) {
        return this.bridges.get(systemId);
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
                this.bridges.put(systemId, new DaliaSessionBridge(session));

                break;
            }
        }
    }

    @Override
    public void destroyed(SmppSession smppSession) {
        if (smppSession.isBound()) {
            String systemId = smppSession.getSystemId();
            this.bridges.remove(systemId);
        }
        else {
            this.pendingSessions.remove(smppSession);
        }
    }
}

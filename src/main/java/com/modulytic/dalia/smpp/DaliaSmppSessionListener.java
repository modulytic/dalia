package com.modulytic.dalia.smpp;

import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.SmppSessionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves {@link SmppSession sessions} on connect and bind, and provides methods to access their {@link DaliaSessionBridge bridges} easily in Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaSmppSessionListener implements SmppSessionListener {
    /**
     * Hashmap of active connections, indexed by username, so we can access them quickly
     */
    private static final Map<String, DaliaSessionBridge> bridges = new ConcurrentHashMap<>();

    /**
     * Sessions that have been initiated, but not yet bound or authenticated
     */
    private static final List<SmppSession> pendingSessions = new ArrayList<>();

    /**
     * Get {@link DaliaSessionBridge bridge} to communicate with an ESME
     * @param systemId  username ESME is authenticated with
     * @return          bridge if found, otherwise null
     */
    public DaliaSessionBridge getSessionBridge(String systemId) {
        return bridges.get(systemId);
    }

    /**
     * handle when SMPP session is created, but may never be bound
     * @param smppSession   SMPP session
     */
    @Override
    public void created(SmppSession smppSession) {
        pendingSessions.add(smppSession);
    }

    /**
     * indicate that session is successfully bound
     * @param systemId  username ESME is authenticated under
     */
    public void activateSession(String systemId) {
        for (SmppSession session : pendingSessions) {
            if (session.isBound() && session.getSystemId().equals(systemId)) {
                pendingSessions.remove(session);
                bridges.put(systemId, new DaliaSessionBridge(session));

                break;
            }
        }
    }

    /**
     * SMPP session is closed
     * @param smppSession   session object
     */
    @Override
    public void destroyed(SmppSession smppSession) {
        if (smppSession.isBound()) {
            String systemId = smppSession.getSystemId();
            bridges.remove(systemId);
        }
        else {
            pendingSessions.remove(smppSession);
        }
    }
}

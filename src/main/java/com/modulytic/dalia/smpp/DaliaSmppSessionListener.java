package com.modulytic.dalia.smpp;

import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.SmppSessionListener;
import net.gescobar.smppserver.packet.Bind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Saves {@link SmppSession sessions} on connect and bind, and provides methods to access their {@link DaliaSessionBridge bridges} easily in Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaSmppSessionListener implements SmppSessionListener {
    /**
     * Hashmap of active connections, indexed by username, so we can access them quickly
     */
    private final HashMap<String, DaliaSessionBridge> bridges;

    /**
     * Sessions that have been initiated, but not yet bound or authenticated
     */
    private final List<SmppSession> pendingSessions;

    public DaliaSmppSessionListener() {
        this.bridges = new HashMap<>();

        this.pendingSessions = new ArrayList<>();
    }

    /**
     * Get {@link DaliaSessionBridge bridge} to communicate with an ESME
     * @param systemId  username ESME is authenticated with
     * @return          bridge if found, otherwise null
     */
    public DaliaSessionBridge getSessionBridge(String systemId) {
        return this.bridges.get(systemId);
    }

    /**
     * handle when SMPP session is created, but may never be bound
     * @param smppSession   SMPP session
     */
    @Override
    public void created(SmppSession smppSession) {
        this.pendingSessions.add(smppSession);
    }

    /**
     * indicate that session is successfully bound, called from {@link SmppRequestHandler#onBind(Bind)}
     * @param systemId  username ESME is authenticated under
     */
    public void activateSession(String systemId) {
        for (SmppSession session : pendingSessions) {
            if (session.isBound() && session.getSystemId().equals(systemId)) {
                this.pendingSessions.remove(session);
                this.bridges.put(systemId, new DaliaSessionBridge(session));

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
            this.bridges.remove(systemId);
        }
        else {
            this.pendingSessions.remove(smppSession);
        }
    }
}

package com.modulytic.dalia.smpp.event;

import com.modulytic.dalia.smpp.DaliaSessionBridge;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.SmppSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves {@link SmppSession sessions} on connect and bind, and provides methods to access their {@link DaliaSessionBridge bridges} easily in Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class DaliaSmppSessionListener implements SmppSessionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DaliaSmppSessionListener.class);

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
    public static DaliaSessionBridge getSessionBridge(String systemId) {
        return bridges.get(systemId);
    }

    public static void deactivate(String smppUser) {
        if (bridges.remove(smppUser) != null) {
            LOGGER.info("Session for user '{}' ended", smppUser);
        }
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static void activate(String systemId) {
        for (SmppSession session : pendingSessions) {
            if (session.isBound() && systemId.equals(session.getSystemId())) {
                LOGGER.info("Activating session for user '{}'", systemId);
                bridges.put(systemId, new DaliaSessionBridge(session));
                pendingSessions.remove(session);

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
        if (!smppSession.isBound())
            pendingSessions.remove(smppSession);
    }
}

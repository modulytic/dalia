package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import net.gescobar.smppserver.SmppException;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools to interact with an SMPP session/ESME easily from Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaSessionBridge {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DaliaSessionBridge.class);
    private final SmppSession session;
    public DaliaSessionBridge(SmppSession sess) {
        session = sess;
    }

    /**
     * Send PDU to connected ESME, with {@link Constants#SMPP_REQUEST_TIMEOUT default timeout}
     * @param request   {@link SmppRequest request}
     * @return          response if received, otherwise null
     */
    public SmppResponse sendGescobarPdu(SmppRequest request) {
        return sendGescobarPdu(request, Constants.SMPP_REQUEST_TIMEOUT);
    }

    /**
     * Send PDU to connected ESME
     * @param request   {@link SmppRequest request}
     * @param timeout   timeout (millis)
     * @return          response if received, otherwise null
     */
    public SmppResponse sendGescobarPdu(SmppRequest request, long timeout) {
        if (session.isBound()) {
            try {
                return session.sendRequest(request, timeout);
            }
            catch (SmppException e) {
                LOGGER.error(e.getMessage());
            }
        }

        return null;
    }

    // TODO implement this with query_sm_resp, cancel_sm_resp, replace_sm_resp
    public void sendDaliaPdu() {
    }
}

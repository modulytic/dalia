package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import net.gescobar.smppserver.SmppException;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;

/**
 * Tools to interact with an SMPP session/ESME easily from Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaSessionBridge {
    private final SmppSession session;
    public DaliaSessionBridge(SmppSession session) {
        this.session = session;
    }

    /**
     * Send PDU to connected ESME, with {@link Constants#SMPP_REQUEST_TIMEOUT default timeout}
     * @param request   {@link SmppRequest request}
     * @return          response if received, otherwise null
     */
    public SmppResponse sendPdu(SmppRequest request) {
        return sendPdu(request, Constants.SMPP_REQUEST_TIMEOUT);
    }

    /**
     * Send PDU to connected ESME
     * @param request   {@link SmppRequest request}
     * @param timeout   timeout (millis)
     * @return          response if received, otherwise null
     */
    public SmppResponse sendPdu(SmppRequest request, long timeout) {
        if (this.session.isBound()) {
            try {
                return this.session.sendRequest(request, timeout);
            }
            catch (SmppException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

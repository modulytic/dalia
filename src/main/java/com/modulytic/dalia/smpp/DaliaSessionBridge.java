package com.modulytic.dalia.smpp;

import com.modulytic.dalia.app.Constants;
import net.gescobar.smppserver.SmppException;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.packet.SmppPacket;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools to interact with an SMPP session/ESME easily from Java
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class DaliaSessionBridge {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DaliaSessionBridge.class);
    private final SmppSession session;
    public DaliaSessionBridge(SmppSession sess) {
        session = sess;
    }

    public SmppResponse sendPdu(SmppRequest request) {
        try {
            if (session.isBound())
                return routePdu(request);
        }
        catch (SmppException e) {
            LOGGER.error("SMPP error", e);
        }

        return null;
    }

    // TODO add support for other types of PDUs
    private SmppResponse routePdu(SmppRequest request) {
        switch (request.getCommandId()) {
            case SmppPacket.DELIVER_SM:
            case SmppPacket.ENQUIRE_LINK:
            case SmppPacket.UNBIND:
                return session.sendRequest(request, Constants.SMPP_REQUEST_TIMEOUT);

            default:
                return null;
        }
    }
}

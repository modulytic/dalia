package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import net.gescobar.smppserver.SmppSession;
import net.gescobar.smppserver.packet.SmppRequest;
import net.gescobar.smppserver.packet.SmppResponse;

public class DaliaSessionBridge {
    private final SmppSession session;
    public DaliaSessionBridge(SmppSession session) {
        this.session = session;
    }

    public SmppResponse sendPdu(SmppRequest request) {
        return sendPdu(request, Constants.SMPP_REQUEST_TIMEOUT);
    }

    public SmppResponse sendPdu(SmppRequest request, long timeout) {
        if (session.isBound()) {
            return session.sendRequest(request, timeout);
        }

        return null;
    }
}

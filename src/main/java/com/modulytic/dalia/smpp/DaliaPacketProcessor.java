package com.modulytic.dalia.smpp;

import com.modulytic.dalia.app.Constants;
import com.modulytic.dalia.app.Filesystem;
import com.modulytic.dalia.smpp.event.DaliaSmppRequestHandler;
import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.SmppRequest;

/**
 * Processor for incoming SMPP packets, hands off to our {@link DaliaSmppRequestHandler}, stored in {@link #router}
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class DaliaPacketProcessor implements PacketProcessor {
    /**
     * Router to process received packets
     */
    private static final DaliaSmppRequestHandler router = new DaliaSmppRequestHandler();

    /**
     * Constructor
     */
    public DaliaPacketProcessor() {
        String confPath = Filesystem.getPrefixFile(Constants.SMPP_CONF_FILENAME);
        JsonAuthenticator authenticator = new JsonAuthenticator(confPath);

        router.setAuthenticator(authenticator);
    }

    @Override
    public void processPacket(SmppRequest smppRequest, ResponseSender responseSender) {
        router.onSmppRequest(smppRequest, responseSender);
    }
}

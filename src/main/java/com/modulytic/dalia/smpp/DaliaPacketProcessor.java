package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.Config;
import com.modulytic.dalia.local.include.DbManager;
import com.modulytic.dalia.ws.WsdServer;
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
    private final DaliaSmppRequestHandler router;

    /**
     * Constructor
     * @param listener  {@link DaliaSmppSessionListener listener} for active sessions
     * @param database  {@link com.modulytic.dalia.local.include.DbManager database} connection
     */
    public DaliaPacketProcessor(DaliaSmppSessionListener listener, DbManager database) {
        String confPath = Config.getPrefixFile(Constants.SMPP_CONF_FILENAME);
        JsonAuthenticator authenticator = new JsonAuthenticator(confPath);

        this.router = new DaliaSmppRequestHandler(database);
        this.router.setListener(listener);
        this.router.setAuthenticator(authenticator);
    }

    public void setDlrUpdateHandler(DLRUpdateHandler handler) {
        this.router.setUpdateHandler(handler);
    }

    /**
     * Pass WebSocket server to {@link DaliaSmppRequestHandler router}
     * @param server    a {@link WsdServer WebSocket server}
     */
    public void setWsdServer(WsdServer server) {
        this.router.setWsdServer(server);
    }

    @Override
    public void processPacket(SmppRequest smppRequest, ResponseSender responseSender) {
        this.router.onSmppRequest(smppRequest, responseSender);
    }
}

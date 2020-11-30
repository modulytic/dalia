package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.Config;
import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.ws.WsdServer;
import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.SmppRequest;

public class DaliaPacketProcessor implements PacketProcessor {
    private final DaliaSmppRequestRouter router;
    public DaliaPacketProcessor(DaliaSmppSessionListener listener, MySqlDbManager database) {
        String confPath = Config.getPrefixFile(Constants.SMPP_CONF_FILENAME);
        JsonAuthenticator authenticator = new JsonAuthenticator(confPath);

        this.router = new DaliaSmppRequestRouter(database);
        this.router.setListener(listener);
        this.router.setAuthenticator(authenticator);
    }

    public void setWsdServer(WsdServer server) {
        this.router.setWsdServer(server);
    }

    @Override
    public void processPacket(SmppRequest smppRequest, ResponseSender responseSender) {
        this.router.onSmppRequest(smppRequest, responseSender);
    }
}

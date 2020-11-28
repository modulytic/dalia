package com.modulytic.dalia.smpp;

import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.Config;
import net.gescobar.smppserver.PacketProcessor;
import net.gescobar.smppserver.ResponseSender;
import net.gescobar.smppserver.packet.SmppRequest;

public class DaliaPacketProcessor implements PacketProcessor {
    private final DaliaSmppRequestRouter router;
    public DaliaPacketProcessor(DaliaSmppSessionListener listener) {
        String confPath = Config.getPrefixFile(Constants.SMPP_CONF_FILENAME);
        DaliaJsonAuthenticator authenticator = new DaliaJsonAuthenticator(confPath);

        this.router = new DaliaSmppRequestRouter(authenticator, listener);
    }

    @Override
    public void processPacket(SmppRequest smppRequest, ResponseSender responseSender) {
        this.router.route(smppRequest, responseSender);
    }
}

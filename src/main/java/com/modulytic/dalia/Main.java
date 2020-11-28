package com.modulytic.dalia;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.modulytic.dalia.smpp.DaliaPacketProcessor;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import net.gescobar.smppserver.SmppServer;

public class Main {
    public static void main(String[] args) {
        DaliaSmppSessionListener sessionListener = new DaliaSmppSessionListener();
        SmppServer server = new SmppServer(Constants.SMPP_HOST_PORT, new DaliaPacketProcessor(sessionListener));

        server.setSessionListener(sessionListener);
        try {
            server.start();

            // when this program dies, kill the server
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (SmppChannelException e) {
            e.printStackTrace();
        }
    }
}

package com.modulytic.dalia;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.smpp.DLRUpdateHandler;
import com.modulytic.dalia.smpp.DaliaPacketProcessor;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import com.modulytic.dalia.ws.WsdMessageHandler;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.WsdServerThread;
import net.gescobar.smppserver.SmppServer;

/**
 * Main class, entrypoint into program
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class Main {
    public static void main(String[] args) {
        // MySQL database
        MySqlDbManager database = new MySqlDbManager(Constants.DB_USERNAME, Constants.DB_PASSWORD);

        // SMPP server
        DaliaSmppSessionListener sessionListener = new DaliaSmppSessionListener();
        DaliaPacketProcessor packetProcessor = new DaliaPacketProcessor(sessionListener, database);
        SmppServer smppServer = new SmppServer(Constants.SMPP_HOST_PORT, packetProcessor);

        // Externally-accessible WebSockets server
        DLRUpdateHandler updateHandler = new DLRUpdateHandler(database, sessionListener);
        packetProcessor.setDlrUpdateHandler(updateHandler);
        WsdMessageHandler wsdMessageHandler = new WsdMessageHandler(updateHandler);
        WsdServer wsdServer = new WsdServer(Constants.WS_HOST_PORT);
        wsdServer.setHandler(wsdMessageHandler);
        packetProcessor.setWsdServer(wsdServer);                // give packet processor access to server
        new WsdServerThread(wsdServer).start();                 // .run() is blocking so we run the WS server on a new thread

        smppServer.setSessionListener(sessionListener);
        try {
            // when this program dies, free our resources
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                smppServer.stop();
                database.close();
            }));

            smppServer.start();
        } catch (SmppChannelException e) {
            e.printStackTrace();
        }
    }
}

package com.modulytic.dalia;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.modulytic.dalia.local.MySqlDbManager;
import com.modulytic.dalia.local.include.DbConstants;
import com.modulytic.dalia.smpp.DLRUpdateHandler;
import com.modulytic.dalia.smpp.DaliaPacketProcessor;
import com.modulytic.dalia.smpp.DaliaSmppSessionListener;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.WsdServerThread;
import net.gescobar.smppserver.SmppServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class, entrypoint into program
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class Main {
    private Main() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // MySQL database
        final MySqlDbManager database = new MySqlDbManager(DbConstants.USERNAME, DbConstants.PASSWORD);
        DaliaContext.setDatabase(database);

        // listener for new SMPP sessions
        final DaliaSmppSessionListener sessionListener = new DaliaSmppSessionListener();
        DaliaContext.setSessionListener(sessionListener);

        // SMPP server
        final DaliaPacketProcessor packetProcessor = new DaliaPacketProcessor();
        SmppServer smppServer = new SmppServer(Constants.SMPP_HOST_PORT, packetProcessor);

        // Handler for incoming message state updates
        final DLRUpdateHandler updateHandler = new DLRUpdateHandler();
        DaliaContext.setDLRUpdateHandler(updateHandler);

        // Externally-accessible WebSockets server
        final WsdServer wsdServer = new WsdServer(Constants.WS_HOST_PORT);
        DaliaContext.setWsdServer(wsdServer);
        new WsdServerThread(wsdServer).start();                 // .run() is blocking so we run the WS server on a new thread

        smppServer.setSessionListener(DaliaContext.getSessionListener());
        try {
            // when this program dies, free our resources
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                smppServer.stop();
                database.close();
            }));

            smppServer.start();
        } catch (SmppChannelException e) {
            LOGGER.error(e.getMessage());
        }
    }
}

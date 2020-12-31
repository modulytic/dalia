package com.modulytic.dalia;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.modulytic.dalia.app.Constants;
import com.modulytic.dalia.app.Context;
import com.modulytic.dalia.app.Filesystem;
import com.modulytic.dalia.app.database.MySqlDatabase;
import com.modulytic.dalia.app.database.include.DatabaseConstants;
import com.modulytic.dalia.smpp.HtpasswdAuthenticator;
import com.modulytic.dalia.smpp.event.DaliaSmppRequestHandler;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import com.modulytic.dalia.smpp.internal.AppSmppServer;
import com.modulytic.dalia.ws.WsdServer;
import com.modulytic.dalia.ws.WsdThreadSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main class, entrypoint into program
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class Main {
    private Main() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // MySQL database
        final MySqlDatabase database = new MySqlDatabase(DatabaseConstants.USERNAME, DatabaseConstants.PASSWORD);
        Context.setDatabase(database);

        // SMPP server
        final AppSmppServer smppServer = new AppSmppServer(Constants.SMPP_HOST_PORT);
        smppServer.setPacketProcessor(DaliaSmppRequestHandler.class);

        // Authenticator for new SMPP users
        final String confPath = Filesystem.getPrefixFile(Constants.SMPP_CONF_FILENAME);
        try {
            SmppRequestHandler.setAuthenticator(new HtpasswdAuthenticator(confPath));
        } catch (IOException e) {
            LOGGER.error("IO Error", e);
        }

        // Externally-accessible WebSockets server
        final WsdServer wsdServer = new WsdServer(Constants.WS_HOST_PORT);
        WsdThreadSpawner.start(wsdServer);

        try {
            // when this program dies, free our resources
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                smppServer.stop();
                database.close();
            }));

            smppServer.start();
        }
        catch (SmppChannelException e) {
            LOGGER.error(e.getMessage());
        }
    }
}

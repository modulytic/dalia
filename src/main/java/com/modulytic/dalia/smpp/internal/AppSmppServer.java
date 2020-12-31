package com.modulytic.dalia.smpp.internal;

import com.modulytic.dalia.app.Constants;
import com.modulytic.dalia.app.Filesystem;
import com.modulytic.dalia.smpp.HtpasswdAuthenticator;
import com.modulytic.dalia.smpp.event.DaliaSmppRequestHandler;
import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import com.modulytic.dalia.smpp.include.SmppRequestHandler;
import net.gescobar.smppserver.SmppServer;

import java.io.IOException;

public class AppSmppServer extends SmppServer {
    static final String confPath = Filesystem.getPrefixFile(Constants.SMPP_CONF_FILENAME);

    public AppSmppServer(int port) throws IOException {
        // DaliaSmppRequestHandler is the class to create handlers for incoming messages from
        super(port, DaliaSmppRequestHandler.class);
        super.setSessionListener(new DaliaSmppSessionListener());

        // Authenticator for new SMPP users
        SmppRequestHandler.setAuthenticator(new HtpasswdAuthenticator(confPath));
    }
}

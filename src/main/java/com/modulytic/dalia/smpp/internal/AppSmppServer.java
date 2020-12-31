package com.modulytic.dalia.smpp.internal;

import com.modulytic.dalia.smpp.event.DaliaSmppSessionListener;
import net.gescobar.smppserver.SmppServer;

public class AppSmppServer extends SmppServer {
    public AppSmppServer(int port) {
        super(port);
        super.setSessionListener(new DaliaSmppSessionListener());
    }
}

package com.modulytic.dalia.smpp.include;

import net.gescobar.smppserver.Response;

public abstract class SmppAuthenticator {
    public abstract Response auth(String username, String password);
}

package com.modulytic.dalia.smpp.include;

import net.gescobar.smppserver.Response;

/**
 * Authenticate users in {@link SmppRequestHandler}
 * @author <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public abstract class SmppAuthenticator {
    /**
     * Authenticate a username and password passed from an ESME
     * @param username  username, string
     * @param password  password, string
     * @return          SMPP response, success (Response.OK) or failure
     */
    public abstract Response auth(String username, String password);
}

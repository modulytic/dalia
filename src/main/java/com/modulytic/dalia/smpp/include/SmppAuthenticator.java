package com.modulytic.dalia.smpp.include;

public abstract class SmppAuthenticator {
    public abstract boolean auth(String username, String password);
}

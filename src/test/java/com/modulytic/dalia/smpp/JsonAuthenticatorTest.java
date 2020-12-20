package com.modulytic.dalia.smpp;

import com.google.common.collect.ImmutableMap;
import net.gescobar.smppserver.Response;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonAuthenticatorTest {
    @Test
    void authWithNoCredentials() {
        JsonAuthenticator authenticator = new JsonAuthenticator(new HashMap<>());

        Response res = authenticator.auth("user", "pass");
        assertEquals(Response.INVALID_SYSTEM_ID, res);
    }

    @Test
    void authWithWrongUsername() {
        Map<String, String> credentials = ImmutableMap.of("smppuser", "password");
        JsonAuthenticator authenticator = new JsonAuthenticator(credentials);

        Response res = authenticator.auth("user", "pass");
        assertEquals(Response.INVALID_SYSTEM_ID, res);
    }

    @Test
    void authWithWrongPassword() {
        Map<String, String> credentials = ImmutableMap.of("smppuser", "password");
        JsonAuthenticator authenticator = new JsonAuthenticator(credentials);

        Response res = authenticator.auth("smppuser", "pass");
        assertEquals(Response.INVALID_PASSWORD, res);
    }

    @Test
    void authNull() {
        Map<String, String> credentials = ImmutableMap.of("smppuser", "password");
        JsonAuthenticator authenticator = new JsonAuthenticator(credentials);

        Response res1 = authenticator.auth(null, "pass");
        assertEquals(Response.MISSING_EXPECTED_PARAMETER, res1);

        Response res2 = authenticator.auth("user", null);
        assertEquals(Response.MISSING_EXPECTED_PARAMETER, res2);

        Response res3 = authenticator.auth(null, null);
        assertEquals(Response.MISSING_EXPECTED_PARAMETER, res3);
    }
}
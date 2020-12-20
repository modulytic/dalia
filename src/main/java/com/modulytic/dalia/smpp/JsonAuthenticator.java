package com.modulytic.dalia.smpp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.modulytic.dalia.smpp.include.SmppAuthenticator;
import net.gescobar.smppserver.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Authenticator that takes JSON as its database of accounts
 * <p>
 *     JSON should be in the following format:
 *     {
 *         "user1": "pass1",
 *         "user2": "pass2"
 *     }
 *
 *     ...and so on
 * </p>
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class JsonAuthenticator extends SmppAuthenticator {
    /**
     * HashMap of credentials, stored in format (K, V): (username, password)
     */
    final private Map<String, String> credentials;

    /**
     * Read text from JSON file, parse with Gson, and call HashMap constructor
     * @param filePath  path to valid JSON data
     * @return          HashMap that can be accepted by {@link #JsonAuthenticator(Map)}
     */
    private static HashMap<String, String> pathToCredentialsMap(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            Type hashMapType = new TypeToken<HashMap<String, String>>(){}.getType();
            return new Gson().fromJson(content, hashMapType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Constructor
     * @param filePath  path to a JSON file
     */
    public JsonAuthenticator(String filePath) {
        this(pathToCredentialsMap(filePath));
    }

    /**
     * Constructor
     * @param credentialMap Parsed JSON data
     */
    public JsonAuthenticator(Map<String, String> credentialMap) {
        super();
        this.credentials = credentialMap;
    }

    @Override
    public Response auth(String username, String password) {
        if (username == null || password == null)
            return Response.MISSING_EXPECTED_PARAMETER;

        String correctPassword = this.credentials.get(username);
        if (correctPassword == null)
            return Response.INVALID_SYSTEM_ID;

        if (password.equals(correctPassword)) {
            return Response.OK;
        }
        else {
            return Response.INVALID_PASSWORD;
        }
    }
}

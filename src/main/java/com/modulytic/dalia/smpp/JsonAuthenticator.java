package com.modulytic.dalia.smpp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.modulytic.dalia.smpp.include.SmppAuthenticator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class JsonAuthenticator extends SmppAuthenticator {
    // <username, password>
    final private HashMap<String, String> credentials;

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

    public JsonAuthenticator(String filePath) {
        this(pathToCredentialsMap(filePath));
    }

    public JsonAuthenticator(HashMap<String, String> credentialMap) {
        super();
        this.credentials = credentialMap;
    }

    @Override
    public boolean auth(String username, String password) {
        if (username == null || password == null)
            return false;

        String correctPassword = this.credentials.get(username);
        return password.equals(correctPassword);
    }
}

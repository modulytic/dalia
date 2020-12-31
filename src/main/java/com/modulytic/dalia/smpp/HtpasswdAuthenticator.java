package com.modulytic.dalia.smpp;

import com.modulytic.dalia.smpp.include.SmppAuthenticator;
import net.gescobar.smppserver.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HtpasswdAuthenticator extends SmppAuthenticator {
    /**
     * HashMap of credentials, stored in format (K, V): (username, password)
     */
    private static final Map<String, String> credentials = new ConcurrentHashMap<>();

    private static final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
    private static final boolean supportPlainText = (os.startsWith("windows") || os.startsWith("netware"));

    public HtpasswdAuthenticator(String path) throws IOException {
        this();
        useFile(path);
    }

    public HtpasswdAuthenticator() {
        super();
    }

    /**
     * Constructor
     * @param credentialMap Parsed JSON data
     */
    public void appendCredentials(Map<String, String> credentialMap) {
        credentials.putAll(credentialMap);
    }

    private static boolean supportsPlainText() {
        return supportPlainText;
    }

    private static boolean supportsCrypt() {
        return !supportsPlainText();
    }

    /**
     * Read text from JSON file, parse with Gson, and call HashMap constructor
     * @param filePath  path to valid JSON data
     */
    public static void useFile(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null)
                    break;
                else if (line.isBlank())         // ignore blank lines, don't crash
                    continue;

                // process line here
                final String[] credLine = line.split(":", 2);
                credentials.put(credLine[0], credLine[1]);
            }
        }
    }

    @Override
    public Response auth(String username, String password) {
        if (username == null || password == null)
            return Response.MISSING_EXPECTED_PARAMETER;

        String hash = credentials.get(username);
        if (hash == null)
            return Response.INVALID_SYSTEM_ID;

        if (validatePassword(password, hash)) {
            return Response.OK;
        }
        else {
            return Response.INVALID_PASSWORD;
        }
    }

    // inspired by: github.com/gitblit/gitblit/blob/master/src/main/java/com/gitblit/auth/HtpasswdAuthProvider.java
    private static boolean validatePassword(String password, String hash) {
        boolean authenticated = false;

        if (hash.startsWith("$apr1$")) {
            if (hash.equals(Md5Crypt.apr1Crypt(password, hash))) {
                authenticated = true;
            }
        }
        else if (hash.startsWith("{SHA}")) {
            String sha = Base64.encodeBase64String(DigestUtils.sha1(password));
            if (hash.substring("{SHA}".length()).equals(sha)) {
                authenticated = true;
            }
        }
        else if (supportsCrypt() && hash.equals(Crypt.crypt(password, hash))) {
            authenticated = true;
        }
        else if (supportsPlainText() && hash.equals(password)) {
            authenticated = true;
        }

        return authenticated;
    }
}

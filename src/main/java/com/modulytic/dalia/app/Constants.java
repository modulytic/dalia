package com.modulytic.dalia.app;

/**
 * Constants relevant to the program that should be easy to change
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public final class Constants {
    private Constants() {}

    /**
     * Environment variable that holds location of prefix
     */
    public static final String ENV_PREFIX = "DALIA_PREFIX";

    /**
     * Environment variable that holds the database host
     */
    public static final String ENV_DATABASE = "DALIA_DB_HOST";

    /**
     * Directory to fall back to if {@link #ENV_PREFIX} is not set
     */
    public static final String PREFIX_FALLBACK = "/root/dalia";

    /**
     * Filename to look for SMPP users in
     */
    public static final String SMPP_CONF_FILENAME = "smpp_users.json";

    /**
     * Port to host SMPP server on
     */
    public static final int SMPP_HOST_PORT = 2775;

    /**
     * hort for WebSocket server to be hosted on
     */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public static final String WSD_HOST_PORT = "0.0.0.0";

    /**
     * Default timeout for sent SMPP requests
     */
    public static final int SMPP_REQUEST_TIMEOUT = 1000;

    /**
     * Default port to host the WebSockets server on
     */
    public static final int WS_HOST_PORT = 3000;

    /**
     * Default region for decoding national numbers
     */
    public static final String DEFAULT_ADDRESS_REGION = "US";
}

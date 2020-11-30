package com.modulytic.dalia;

/**
 * Constants relevant to the program that should be easy to change
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class Constants {
    /**
     * Environment variable that holds location of prefix
     */
    public static String ENV_PREFIX = "DALIA_PREFIX";

    /**
     * Environment variable that holds the database host
     */
    public static String ENV_DATABASE = "DALIA_DB_HOST";

    /**
     * Directory to fall back to if {@link #ENV_PREFIX} is not set
     */
    public static String PREFIX_FALLBACK = "/root/dalia";

    /**
     * Filename to look for SMPP users in
     */
    public static String SMPP_CONF_FILENAME = "smpp_users.json";

    /**
     * Port to host SMPP server on
     */
    public static int SMPP_HOST_PORT = 2775;

    /**
     * Default timeout for sent SMPP requests
     */
    public static int SMPP_REQUEST_TIMEOUT = 1000;

    /**
     * Default name of MySQL database
     */
    public static String DB_DEFAULT_NAME = "dalia";

    /**
     * Default username for MySQL
     */
    public static String DB_USERNAME = "dalia";

    /**
     * Default password for MySQL
     */
    public static String DB_PASSWORD = "password";

    /**
     * Default port to host the WebSockets server on
     */
    public static int WS_HOST_PORT = 3000;
}

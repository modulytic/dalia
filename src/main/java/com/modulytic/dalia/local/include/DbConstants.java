package com.modulytic.dalia.local.include;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DbConstants {
    private DbConstants() {}

    /**
     * Default name of MySQL database
     */
    public static final String DEFAULT_NAME = "dalia";
    /**
     * Default username for MySQL
     */
    public static final String USERNAME = "dalia";
    /**
     * Default password for MySQL
     */
    public static final String PASSWORD = "password";

    /**
     * Name of SQL table
     */
    public static final String DLR_TABLE = "dlr_status";

    public static final String MSG_STATUS   = "msg_status";
    public static final String MSG_ID       = "msg_id";
    public static final String SMPP_USER    = "smpp_user";
    public static final String FAILURE_ONLY = "failure_only";
    public static final String INTERMEDIATE = "intermediate";
    public static final String SUBMIT_DATE  = "submit_date";
    public static final String SOURCE_ADDR  = "src_addr";
    public static final String DEST_ADDR    = "dst_addr";

    public static final String VROUTE_TABLE = "billing_vroutes";
    public static final String COUNTRY_CODE = "country_code";
    public static final String IS_ACTIVE    = "is_active";

    public static final String LOG_TABLE = "billing_logs";
    public static final String VROUTE    = "vroute";
    public static final String RATE      = "rate";

    private static final Map<String, Class<?>> dbTypes = new ConcurrentHashMap<>();
    public static Map<String, Class<?>> getDbTypes() {
        if (dbTypes.isEmpty()) {
            dbTypes.put(MSG_ID, String.class);
            dbTypes.put(MSG_STATUS, String.class);
            dbTypes.put(FAILURE_ONLY, Boolean.class);
            dbTypes.put(INTERMEDIATE, Boolean.class);
            dbTypes.put(SUBMIT_DATE, Timestamp.class);
            dbTypes.put(SOURCE_ADDR, String.class);
            dbTypes.put(DEST_ADDR, String.class);
            dbTypes.put(SMPP_USER, String.class);
        }

        return dbTypes;
    }
}

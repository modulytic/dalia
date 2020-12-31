package com.modulytic.dalia.app.database.include;

public final class DatabaseConstants {
    private DatabaseConstants() {}

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
    public static final String VROUTE_ID    = "id";
    public static final String VROUTE_NAME  = "vroute_name";
    public static final String COUNTRY_CODE = "country_code";
    public static final String IS_ACTIVE    = "is_active";

    public static final String LOG_TABLE = "billing_logs";
    public static final String VROUTE    = "vroute";
    public static final String RATE      = "rate";
    
    public static final int TRUE  = 1;
    public static final int FALSE = 0;
}

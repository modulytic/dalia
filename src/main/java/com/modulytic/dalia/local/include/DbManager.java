package com.modulytic.dalia.local.include;

import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

/**
 * Abstract class for interfacing with persistent databases
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public abstract class DbManager {
    protected final Logger LOGGER;

    private final Map<String, Class<?>> requiredKeys;

    public DbManager() {
        LOGGER = LoggerFactory.getLogger(this.getClass());

        requiredKeys = new HashMap<>();
        requiredKeys.put("msg_id", String.class);
        requiredKeys.put("msg_status", String.class);
        requiredKeys.put("failure_only", Boolean.class);
        requiredKeys.put("intermediate", Boolean.class);
        requiredKeys.put("submit_date", Timestamp.class);
        requiredKeys.put("src_addr", String.class);
        requiredKeys.put("dst_addr", String.class);
        requiredKeys.put("smpp_user", String.class);
    }

    public Map<String, Class<?>> getRequiredKeys() {
        return this.requiredKeys;
    }

    /**
     * Fetch all columns from database
     * @param table Name of table to fetch from
     * @param match Parameters to match
     * @return      List of maps, where each map is a row
     */
    public Table<Integer, String, Object> fetch(String table, LinkedHashMap<String, ?> match) {
        return fetch(table, match, null);
    }

    /**
     * Insert new values into database
     * @param table     Name of table
     * @param values    Values to insert
     */
    public abstract void insert(String table, LinkedHashMap<String, ?> values);

    /**
     * Update values that already exist in database
     * @param table     Name of table
     * @param values    New values to update
     * @param match     Rows to match to perform the update on
     */
    public abstract void update(String table, LinkedHashMap<String, ?> values, LinkedHashMap<String, ?> match);

    /**
     * Fetch certain columns from database
     * @param table     Name of table to fetch from
     * @param match     Parameters to match
     * @param columns   Columns to return
     * @return          List of maps, where each map is a row
     */
    public abstract Table<Integer, String, Object> fetch(String table, LinkedHashMap<String, ?> match, Set<String> columns);

    /**
     * If connection to database needs to be manually closed, that can be implemented here
     */
    public abstract void close();
}

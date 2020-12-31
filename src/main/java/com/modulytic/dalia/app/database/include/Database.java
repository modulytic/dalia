package com.modulytic.dalia.app.database.include;

import com.modulytic.dalia.app.database.DatabaseResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Abstract class for interfacing with persistent databases
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public abstract class Database {
    protected final transient Logger LOGGER;

    public Database() {
        LOGGER = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Fetch all columns from database
     * @param table Name of table to fetch from
     * @param match Parameters to match
     * @return      List of maps, where each map is a row
     */
    public DatabaseResults fetch(String table, Map<String, ?> match) {
        return fetch(table, match, null);
    }

    /**
     * Insert new values into database
     * @param table     Name of table
     * @param values    Values to insert
     */
    public abstract void insert(String table, Map<String, ?> values);

    /**
     * Update values that already exist in database
     * @param table     Name of table
     * @param values    New values to update
     * @param match     Rows to match to perform the update on
     */
    public abstract void update(String table, Map<String, ?> values, Map<String, ?> match);

    /**
     * Fetch certain columns from database
     * @param table     Name of table to fetch from
     * @param match     Parameters to match
     * @param columns   Columns to return
     * @return          List of maps, where each map is a row
     */
    public abstract DatabaseResults fetch(String table, Map<String, ?> match, Set<String> columns);

    /**
     * If connection to database needs to be manually closed, that can be implemented here
     */
    public abstract void close();
}

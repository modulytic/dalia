package com.modulytic.dalia.local.include;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DbManager {
    protected final Logger LOGGER;
    public DbManager() {
        LOGGER = LoggerFactory.getLogger(this.getClass());
    }

    public List<HashMap<String, ?>> fetch(String table, Map<String, ?> match) {
        return fetch(table, match, null);
    }

    public abstract void insert(String table, Map<String, ?> values);
    public abstract void update(String table, Map<String, ?> values, Map<String, ?> match);
    public abstract List<HashMap<String, ?>> fetch(String table, Map<String, ?> match, Set<String> columns);
    public abstract void close();
}

package com.modulytic.dalia.app.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RowStore {
    private final Map<String, Object> row     = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> types = new ConcurrentHashMap<>();

    public <T> T get(String col) {
        T gotten = (T) row.get(col);

        if (gotten == null)
            return null;

        final Class<?> genericClass = gotten.getClass();
        final Class<?> correctClass = types.get(col);

        // make sure we can actually do this cast
        if (genericClass.isAssignableFrom(correctClass)) {
            return gotten;
        }
        else {
            throw new ClassCastException(String.format("Casting %s to %s", correctClass.getName(), genericClass.getName()));
        }
    }

    public Class<?> getType(String col) {
        return types.get(col);
    }

    public <T> void add(String col, T value) {
        add(col, value, (Class<T>) value.getClass());
    }

    public <T> void add(String col, T value, Class<? extends T> type) {
        row.put(col, value);
        types.put(col, type);
    }

    public <T> void replace(String col, T value) {
        replace(col, value, (Class<T>) value.getClass());
    }

    public <T> void replace(String col, T value, Class<? extends T> type) {
        row.replace(col, value);
        types.replace(col, type);
    }

    public Map<String, Object> toMap() {
        return row;
    }

    public boolean isEmpty() {
        return row.isEmpty();
    }
}

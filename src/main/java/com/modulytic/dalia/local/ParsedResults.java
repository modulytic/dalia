package com.modulytic.dalia.local;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.modulytic.dalia.local.include.DbConstants;

import java.sql.*;
import java.util.Map;

public class ParsedResults {
    private static final Map<String, Class<?>> classNames = DbConstants.getDbTypes();

    private final ResultSet resultSet;
    private final ResultSetMetaData metaData;
    private final int numColumns;

    private final Table<Integer, String, Object> results = HashBasedTable.create();

    public ParsedResults(ResultSet results) throws SQLException {
        resultSet = results;
        metaData = resultSet.getMetaData();
        numColumns = metaData.getColumnCount();

        for (int i = 0; resultSet.next(); i++) {
            addRow(i);
        }

        resultSet.close();
    }

    private void addRow(int rowNum) throws SQLException {
        for (int i = 0; i < numColumns; i++) {
            String columnName = metaData.getColumnName(i);
            Class<?> columnType = classNames.get(columnName);

            if (columnType.isInstance(String.class)) {
                results.put(rowNum, columnName, resultSet.getString(i));
            }
            else if (columnType.isInstance(Timestamp.class)) {
                Timestamp ts = resultSet.getTimestamp(i);
                results.put(rowNum, columnName, ts.toLocalDateTime());
            }
            else if (columnType.isInstance(Date.class)) {
                Date date = resultSet.getDate(i);
                results.put(rowNum, columnName, date.toLocalDate());
            }
            else if (columnType.isInstance(Boolean.class)) {
                results.put(rowNum, columnName, resultSet.getBoolean(i));
            }
            else {
                results.put(rowNum, columnName, resultSet.getObject(i));
            }
        }
    }

    public Table<Integer, String, Object> toTable() {
        if (results.isEmpty())
            return null;

        return results;
    }
}

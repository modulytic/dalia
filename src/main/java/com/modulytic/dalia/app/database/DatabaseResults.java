package com.modulytic.dalia.app.database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseResults {
    private final ResultSet resultSet;
    private final ResultSetMetaData metaData;
    private final int numColumns;

    private final List<RowStore> rows = new ArrayList<>();

    public DatabaseResults(ResultSet results) throws SQLException {
        resultSet = results;
        metaData = resultSet.getMetaData();
        numColumns = metaData.getColumnCount();

        while (resultSet.next())
            rsAddNextRow();

        resultSet.close();
    }

    public DatabaseResults() {
        resultSet = null;
        metaData = null;
        numColumns = 0;
    }

    public void add(RowStore row) {
        rows.add(row);
    }

    private void rsAddNextRow() throws SQLException {
        if (resultSet == null || resultSet.isClosed() || numColumns <= 0)
            return;

        RowStore newRow = new RowStore();
        for (int i = 1; i <= numColumns; i++) {
            String columnName = metaData.getColumnName(i);
            int columnType = metaData.getColumnType(i);

            switch (columnType) {
                case Types.TIMESTAMP:
                    Timestamp ts = resultSet.getTimestamp(i);
                    newRow.add(columnName, ts.toLocalDateTime(), LocalDateTime.class);
                    break;

                case Types.DATE:
                    Date date = resultSet.getDate(i);
                    newRow.add(columnName, date.toLocalDate(), LocalDate.class);
                    break;

                default:
                    Object o = resultSet.getObject(i);
                    newRow.add(columnName, o, o.getClass());
                    break;
            }
        }

        rows.add(newRow);
    }

    public RowStore row(int i) {
        return rows.get(i);
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}

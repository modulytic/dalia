package com.modulytic.dalia.local;

import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.include.DbManager;

import java.sql.*;
import java.util.*;

public class MySqlDbManager extends DbManager {
    private Connection connection;

    public MySqlDbManager(String user, String pass) {
        this(user, pass, Constants.DB_DEFAULT_NAME);
    }

    public MySqlDbManager(String user, String pass, String database) {
        this(user, pass, database, getDefaultHost(), 3306);
    }

    private static String getDefaultHost() {
        String dbHost = System.getenv(Constants.ENV_DATABASE);
        if (dbHost == null) {
            dbHost = "localhost";
        }

        return dbHost;
    }

    public MySqlDbManager(String user, String pass, String database, String host, int port) {
        super();

        String url = buildConnectionUrl(user, pass, database, host, port);
        try {
            this.connection = DriverManager.getConnection(url);
            LOGGER.info("Successfully connected to database");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private String buildConnectionUrl(String user, String pass, String database, String host, int port) {
        return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, database, user, pass);
    }

    // format Java key-value pairs so that we can insert them easily into SQL queries
    private HashMap<String, StringBuilder> generateSqlLists(Map<String, ?> values) {
        StringBuilder columnsList = new StringBuilder();
        StringBuilder valuesList  = new StringBuilder();

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            // add our column
            if (columnsList.length() > 0)
                columnsList.append(", ");

            columnsList.append(entry.getKey());

            // add our value
            if (valuesList.length() > 0)
                valuesList.append(", ");

            Object value = entry.getValue();
            if (value instanceof String) {
                valuesList.append("'");
                valuesList.append(value);
                valuesList.append("'");
            }
            else {
                valuesList.append(value);
            }
        }

        HashMap<String, StringBuilder> res = new HashMap<>();
        res.put("columns", columnsList);
        res.put("rows", valuesList);

        return res;
    }

    private String generateSqlTests(Map<String, ?> match) {
        StringBuilder whereClause = new StringBuilder();
        for (Map.Entry<String, ?> entry : match.entrySet()) {
            if (whereClause.length() > 0)
                whereClause.append(" AND ");

            whereClause
                    .append(entry.getKey())
                    .append("=");

            if (entry.getValue() instanceof String) {
                whereClause.append("'");
                whereClause.append(entry.getValue());
                whereClause.append("'");
            }
            else {
                whereClause.append(entry.getValue());
            }

        }

        return whereClause.toString();
    }

    private String generateColumnList(Set<String> cols) {
        StringBuilder columnsList = new StringBuilder();

        for (String key : cols) {
            // add our column
            if (columnsList.length() > 0)
                columnsList.append(", ");

            columnsList.append(key);
        }

        return columnsList.toString();
    }

    @Override
    public void insert(String table, Map<String, ?> values) {
        HashMap<String, StringBuilder> columns = generateSqlLists(values);

        String query = String.format("INSERT INTO %s (%s) VALUES (%s);", table, columns.get("columns"), columns.get("rows"));
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        // ignore
    }

    private List<HashMap<String, ?>> parseResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int cols = rsMetaData.getColumnCount();

        ArrayList<HashMap<String, ?>> results = new ArrayList<>();

        while (resultSet.next()) {
            HashMap<String, Object> row = new HashMap<>();
            for (int i = 0; i <= cols; i++) {
                row.put(rsMetaData.getColumnName(i), resultSet.getObject(i));
            }

            results.add(row);
        }

        resultSet.close();

        if (results.isEmpty())
            return null;

        return results;
    }

    @Override
    public List<HashMap<String, ?>> fetch(String table, Map<String, ?> match, Set<String> columns) {
        String whereClause = generateSqlTests(match);

        String colsRaw = "*";
        if (columns != null)
            colsRaw = generateColumnList(columns);

        String query = String.format("SELECT %s FROM %s WHERE %s;", colsRaw, table, whereClause);
        try (Statement statement = this.connection.createStatement()) {
            if (statement.execute(query)) {
                return parseResultSet(statement.getResultSet());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        // ignore

        return null;
    }

    @Override
    public void update(String table, Map<String, ?> values, Map<String, ?> match) {
        String newValues   = generateSqlTests(values);
        String whereClause = generateSqlTests(match);

        String query = String.format("UPDATE %s SET %s WHERE %s;", table, newValues, whereClause);
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        // ignore
    }

    @Override
    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException ignored) { }
        }
    }
}

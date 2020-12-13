package com.modulytic.dalia.local;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.include.DbManager;

import java.sql.*;
import java.util.*;

// TODO sanitize all DB inputs!!!

/**
 * {@link DbManager} for MySql
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class MySqlDbManager extends DbManager {
    private Connection connection;

    /**
     * Connect with default network and database params
     * @param user  MySQL username
     * @param pass  MySQL password
     */
    public MySqlDbManager(String user, String pass) {
        this(user, pass, Constants.DB_DEFAULT_NAME);
    }

    /**
     * Connect with default network params
     * @param user      MySQL username
     * @param pass      MySQL password
     * @param database  MySQL database name
     */
    public MySqlDbManager(String user, String pass, String database) {
        this(user, pass, database, getDefaultHost(), 3306);
    }

    /**
     * Read {@link Constants#ENV_DATABASE environment variable} for DB url, or default to localhost
     * @return  String of database URL
     */
    private static String getDefaultHost() {
        String dbHost = System.getenv(Constants.ENV_DATABASE);
        if (dbHost == null) {
            dbHost = "localhost";
        }

        return dbHost;
    }

    /**
     * Connect to MySQL database
     * @param user      MySQL username
     * @param pass      MySQL password
     * @param database  MySQL database name
     * @param host      MySQL database host
     * @param port      MySQL database port
     */
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

    /**
     * Create JBDC URL for database
     * @param user      MySQL username
     * @param pass      MySQL password
     * @param database  MySQL database name
     * @param host      MySQL database host
     * @param port      MySQL database port
     * @return          String, valid JBDC URL
     */
    private String buildConnectionUrl(String user, String pass, String database, String host, int port) {
        return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, database, user, pass);
    }

    /**
     * Format Java key-value pairs so that we can insert them easily into SQL queries
     * <p>
     *    Format for columns:  key1, key2, key3 <br>
     *    Format for values: val1, val2, val3
     * </p>
     * <p>
     *    Also checks if values are strings, and encloses it in quotes if this is the case
     * </p>
     * @param values    Map of values and keys
     * @return          Map with two values: "columns" and "values", which can be directly inserted into a query
     */
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

    /**
     * Format Java key-value pairs so we can easily insert them into SQL queries
     * <p>
     *     Output format: key1=val1, key2=val2 <br>
     *     Will enclose values in quotes if it is a string
     * </p>
     * @param match Map of keys and values
     * @return      String that can be directly inserted into SQL query
     */
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

    /**
     * Generate comma-separated list of columns
     * @param cols  set of column names
     * @return      String that can be directly inserted into SQL query
     */
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
    }

    /**
     * Turn SQL {@link ResultSet} into a list of maps that does not require an open database connection
     * @param resultSet     ResultSet returned from SQL {@link Statement}
     * @return              List of Maps, each map representing a row
     * @throws SQLException any of the operations on ResultSet can throw an {@link SQLException}
     */
    private Table<Integer, String, Object> parseResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int cols = rsMetaData.getColumnCount();

        Table<Integer, String, Object> results = HashBasedTable.create();

        Map<String, Class<?>> classNames = getRequiredKeys();

        // i is rows, j is columns
        for (int i = 0; resultSet.next(); i++) {
            for (int j = 0; j <= cols; j++) {
                String columnName = rsMetaData.getColumnName(j);
                Class<?> columnType = classNames.get(columnName);

                if (columnType.isInstance(String.class)) {
                    results.put(i, columnName, resultSet.getString(j));
                }
                else if (columnType.isInstance(Timestamp.class)) {
                    results.put(i, columnName, resultSet.getTimestamp(j));
                }
                else if (columnType.isInstance(Boolean.class)) {
                    results.put(i, columnName, resultSet.getBoolean(j));
                }
                else {
                    results.put(i, columnName, resultSet.getObject(j));
                }
            }
        }

        resultSet.close();

        if (results.isEmpty())
            return null;

        return results;
    }

    @Override
    public Table<Integer, String, Object> fetch(String table, Map<String, ?> match, Set<String> columns) {
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

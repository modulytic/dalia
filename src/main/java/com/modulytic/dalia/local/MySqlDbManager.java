package com.modulytic.dalia.local;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.modulytic.dalia.Constants;
import com.modulytic.dalia.local.include.DbManager;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
     * Constructor used for testing
     * @param connection    Database connection object
     */
    public MySqlDbManager(Connection connection) {
        this.connection = connection;
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

    private PreparedStatement prepareStatement(String query, LinkedHashMap<String, ?> values) throws SQLException {
        return prepareStatement(query, values, null);
    }

    private PreparedStatement prepareStatement(String query, LinkedHashMap<String, ?> values, LinkedHashMap<String, ?> matches) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(query);

        List<Object> valuesArr = new ArrayList<>(values.values());

        if (matches != null)
            valuesArr.addAll(matches.values());

        for (int i = 0; i < valuesArr.size(); i++) {
            Object value = valuesArr.get(i);

            if (value == null) {
                statement.setNull(i+1, Types.NULL);
            }
            else if (value instanceof String) {
                statement.setString(i+1, (String) value);
            }
            else if (value instanceof Integer) {
                statement.setInt(i+1, (int) value);
            }
            else if (value instanceof Float) {
                statement.setFloat(i+1, (float) value);
            }
            else if (value instanceof Double) {
                statement.setDouble(i+1, (double) value);
            }
            else if (value instanceof Boolean) {
                statement.setBoolean(i+1, (boolean) value);
            }
            else if (value instanceof LocalDateTime) {
                LocalDateTime dt = (LocalDateTime) value;
                statement.setString(i+1, dt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
            }
            else if (value instanceof LocalDate) {
                LocalDate dt = (LocalDate) value;
                statement.setString(i+1, dt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd")));
            }
            else {
                statement.setObject(i+1, value);
            }
        }

        return statement;
    }

    @Override
    public void insert(String table, LinkedHashMap<String, ?> values) {
        String columnsStr = String.join(",", values.keySet());

        // same number of ?s as params, separated by commas
        String valuesPlaceholder = "?, ".repeat(values.size());
        valuesPlaceholder = valuesPlaceholder.substring(0, valuesPlaceholder.length()-2);

        String query = String.format("INSERT INTO %s (%s) VALUES (%s);", table, columnsStr, valuesPlaceholder);
        try (PreparedStatement statement = prepareStatement(query, values)) {
            statement.executeUpdate();
        }
        catch (SQLException throwables) {
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
                    Timestamp ts = resultSet.getTimestamp(j);
                    results.put(i, columnName, ts.toLocalDateTime());
                }
                else if (columnType.isInstance(Date.class)) {
                    Date date = resultSet.getDate(j);
                    results.put(i, columnName, date.toLocalDate());
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
    public Table<Integer, String, Object> fetch(String table, LinkedHashMap<String, ?> matches, Set<String> columns) {
        StringBuilder matchStr = new StringBuilder();
        for (String match : matches.keySet()) {
            if (matchStr.length() != 0)
                matchStr.append(" AND ");

            matchStr.append(match);
            matchStr.append("=?");
        }

        String colsRaw = "*";
        if (columns != null)
            colsRaw = String.join(",", columns);

        String query = String.format("SELECT %s FROM %s WHERE %s;", colsRaw, table, matchStr);
        try (PreparedStatement statement = prepareStatement(query, matches)) {
            if (statement.execute()) {
                return parseResultSet(statement.getResultSet());
            }
        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(String table, LinkedHashMap<String, ?> values, LinkedHashMap<String, ?> matches) {
        StringBuilder updateStr = new StringBuilder();
        for (String value : values.keySet()) {
            if (updateStr.length() != 0)
                updateStr.append(", ");

            updateStr.append(value);
            updateStr.append("=?");
        }

        StringBuilder matchStr = new StringBuilder();
        for (String match : matches.keySet()) {
            if (matchStr.length() != 0)
                matchStr.append(" AND ");

            matchStr.append(match);
            matchStr.append("=?");
        }

        String query = String.format("UPDATE %s SET %s WHERE %s;", table, updateStr, matchStr);
        try (PreparedStatement statement = prepareStatement(query, values, matches)) {
            statement.executeUpdate();
        }
        catch (SQLException throwables) {
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

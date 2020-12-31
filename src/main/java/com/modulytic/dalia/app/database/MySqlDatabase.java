package com.modulytic.dalia.app.database;

import com.modulytic.dalia.app.Constants;
import com.modulytic.dalia.app.database.include.Database;
import com.modulytic.dalia.app.database.include.DatabaseConstants;

import java.sql.*;
import java.util.*;

/**
 * {@link Database} for MySql
 * @author  <a href="mailto:noah@modulytic.com">Noah Sandman</a>
 */
public class MySqlDatabase extends Database {
    private transient Connection connection;

    /**
     * Connect with default network and database params
     * @param user  MySQL username
     * @param pass  MySQL password
     */
    public MySqlDatabase(String user, String pass) {
        this(user, pass, DatabaseConstants.DEFAULT_NAME);
    }

    /**
     * Connect with default network params
     * @param user      MySQL username
     * @param pass      MySQL password
     * @param database  MySQL database name
     */
    public MySqlDatabase(String user, String pass, String database) {
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
    public MySqlDatabase(String user, String pass, String database, String host, int port) {
        super();

        String url = buildConnectionUrl(user, pass, database, host, port);
        try {
            this.connection = DriverManager.getConnection(url);
            LOGGER.info("Successfully connected to database");
        } catch (SQLException throwables) {
            LOGGER.error(throwables.getMessage());
        }
    }

    /**
     * Constructor used for testing
     * @param connection    Database connection object
     */
    public MySqlDatabase(Connection connection) {
        this.connection = connection;
    }

    /**
     * Create JDBC URL for database
     * @param user      MySQL username
     * @param pass      MySQL password
     * @param database  MySQL database name
     * @param host      MySQL database host
     * @param port      MySQL database port
     * @return          String, valid JDBC URL
     */
    private static String buildConnectionUrl(String user, String pass, String database, String host, int port) {
        return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, database, user, pass);
    }

    private QueryStatement prepareStatement(String query, Map<String, ?> values) throws SQLException {
        return prepareStatement(query, values, null);
    }

    @SuppressWarnings("PMD.CloseResource")      // PreparedStatement is closed when QueryStatement is
    private QueryStatement prepareStatement(String query, Map<String, ?> values, Map<String, ?> matches) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(query);
        QueryStatement statement = new QueryStatement(preparedStatement);

        List<Object> valuesArr = new ArrayList<>(values.values());

        if (matches != null)
            valuesArr.addAll(matches.values());

        for (Object value : valuesArr) {
            statement.add(value);
        }

        return statement;
    }

    @Override
    public void insert(String table, Map<String, ?> values) {
        String columnsStr = String.join(",", values.keySet());

        // same number of ?s as params, separated by commas
        String valuesPlaceholder = "?, ".repeat(values.size());
        valuesPlaceholder = valuesPlaceholder.substring(0, valuesPlaceholder.length()-2);

        final String query = String.format("INSERT INTO %s (%s) VALUES (%s);", table, columnsStr, valuesPlaceholder);
        try (QueryStatement statement = prepareStatement(query, values)) {
            statement.update();
        }
        catch (SQLException throwables) {
            LOGGER.error("SQL ERROR", throwables);
        }
    }

    @Override
    public DatabaseResults fetch(String table, Map<String, ?> matches, Set<String> columns) {
        StringBuilder matchStr = new StringBuilder();
        for (String match : matches.keySet()) {
            if (matchStr.length() != 0)
                matchStr.append(" AND ");

            matchStr.append(match);
            matchStr.append("=?");
        }

        String colsRaw = (columns == null) ? "*" : String.join(",", columns);

        final String query = String.format("SELECT %s FROM %s WHERE %s;", colsRaw, table, matchStr);
        try (QueryStatement statement = prepareStatement(query, matches)) {
            return statement.execute();
        }
        catch (SQLException throwables) {
            LOGGER.error("SQL ERROR", throwables);
        }

        return null;
    }

    @Override
    public void update(String table, Map<String, ?> values, Map<String, ?> matches) {
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
        try (QueryStatement statement = prepareStatement(query, values, matches)) {
            statement.update();
        }
        catch (SQLException throwables) {
            LOGGER.error("SQL ERROR", throwables);
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

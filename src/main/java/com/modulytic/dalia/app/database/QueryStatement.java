package com.modulytic.dalia.app.database;

import com.modulytic.dalia.app.database.include.Updateable;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueryStatement implements AutoCloseable, Updateable {
    private final PreparedStatement statement;
    private transient int paramIndex = 1;
    public QueryStatement(PreparedStatement statement) {
        this.statement = statement;
    }

    private void postAdd() {
        paramIndex++;
    }

    @Override
    public void add(String s) throws SQLException {
        statement.setString(paramIndex, s);
        postAdd();
    }

    @Override
    public void add(int i) throws SQLException {
        statement.setInt(paramIndex, i);
        postAdd();
    }

    @Override
    public void add(float f) throws SQLException {
        statement.setFloat(paramIndex, f);
        postAdd();
    }

    @Override
    public void add(double d) throws SQLException {
        statement.setDouble(paramIndex, d);
        postAdd();
    }

    @Override
    public void add(boolean b) throws SQLException {
        statement.setBoolean(paramIndex, b);
        postAdd();
    }

    @Override
    public void add(LocalDateTime dt) throws SQLException {
        statement.setString(paramIndex, dt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
        postAdd();
    }

    @Override
    public void add(LocalDate d) throws SQLException {
        statement.setString(paramIndex, d.format(DateTimeFormatter.ofPattern("uuuu-MM-dd")));
        postAdd();
    }

    @Override
    public void addNull() throws SQLException {
        this.statement.setNull(paramIndex, Types.NULL);
    }

    @Override
    public void add(Object o) throws SQLException {
        if (o == null)                       addNull();
        else if (o instanceof String)        add((String) o);
        else if (o instanceof Integer)       add((int) o);
        else if (o instanceof Float)         add((float) o);
        else if (o instanceof Double)        add((double) o);
        else if (o instanceof Boolean)       add((boolean) o);
        else if (o instanceof LocalDateTime) add((LocalDateTime) o);
        else if (o instanceof LocalDate)     add((LocalDate) o);
        else {
            statement.setObject(paramIndex, o);
            postAdd();
        }
    }

    public DatabaseResults execute() throws SQLException {
        if (statement.execute()) {
            return new DatabaseResults(statement.getResultSet());
        }

        return null;
    }

    public void update() throws SQLException {
        statement.executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        statement.close();
    }
}

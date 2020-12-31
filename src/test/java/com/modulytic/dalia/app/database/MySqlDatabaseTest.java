package com.modulytic.dalia.app.database;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MySqlDatabaseTest {
    private LinkedHashMap<String, Object> getDbParams() {
        // TODO make database manager escape single quotes
        LocalDateTime timestamp = LocalDateTime.of(2001, 6, 27, 1, 5, 45, 0);
        LocalDate date = LocalDate.of(2001, 6, 27);

        // preserve insertion order with LinkedHashMap
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("str", "hello");
        values.put("nullable", null);
        values.put("int", 1);
        values.put("float", 0.5f);
        values.put("bool", true);
        values.put("timestamp", timestamp);
        values.put("date", date);

        return values;
    }

    @Test
    void insert() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        MySqlDatabase database = new MySqlDatabase(connection);

        LinkedHashMap<String, Object> values = getDbParams();

        String expectedQuery = "INSERT INTO test (str,nullable,int,float,bool,timestamp,date) VALUES (?, ?, ?, ?, ?, ?, ?);";
        database.insert("test", values);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(argument.capture());
        assertEquals(expectedQuery, argument.getValue());

        // string and LocalDate and LocalDateTime
        verify(statement, times(3)).setString(anyInt(), anyString());
        verify(statement, times(1)).setNull(anyInt(), anyInt());
        verify(statement, times(1)).setInt(anyInt(), anyInt());
        verify(statement, times(1)).setBoolean(anyInt(), anyBoolean());
        verify(statement, times(1)).setFloat(anyInt(), anyFloat());
        verify(statement, times(1)).executeUpdate();
    }

    @Test
    void fetchAll() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        MySqlDatabase database = new MySqlDatabase(connection);

        LinkedHashMap<String, Object> values = getDbParams();

        String expectedQuery = "SELECT * FROM test WHERE str=? AND nullable=? AND int=? AND float=? AND bool=? AND timestamp=? AND date=?;";
        database.fetch("test", values);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(argument.capture());
        assertEquals(expectedQuery, argument.getValue());

        // string and LocalDate and LocalDateTime
        verify(statement, times(3)).setString(anyInt(), anyString());
        verify(statement, times(1)).setNull(anyInt(), anyInt());
        verify(statement, times(1)).setInt(anyInt(), anyInt());
        verify(statement, times(1)).setBoolean(anyInt(), anyBoolean());
        verify(statement, times(1)).setFloat(anyInt(), anyFloat());
        verify(statement, times(1)).execute();
    }

    @Test
    void fetchColumns() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        MySqlDatabase database = new MySqlDatabase(connection);

        LinkedHashMap<String, Object> values = getDbParams();
        Set<String> columns = ImmutableSet.of("col1", "col2");

        String expectedQuery = "SELECT col1,col2 FROM test WHERE str=? AND nullable=? AND int=? AND float=? AND bool=? AND timestamp=? AND date=?;";
        database.fetch("test", values, columns);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(argument.capture());
        assertEquals(expectedQuery, argument.getValue());

        // string and LocalDate and LocalDateTime
        verify(statement, times(3)).setString(anyInt(), anyString());
        verify(statement, times(1)).setNull(anyInt(), anyInt());
        verify(statement, times(1)).setInt(anyInt(), anyInt());
        verify(statement, times(1)).setBoolean(anyInt(), anyBoolean());
        verify(statement, times(1)).setFloat(anyInt(), anyFloat());
        verify(statement, times(1)).execute();
    }

    @Test
    void update() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        MySqlDatabase database = new MySqlDatabase(connection);

        LinkedHashMap<String, Object> values = getDbParams();

        String expectedQuery = "UPDATE test SET str=?, nullable=?, int=?, float=?, bool=?, timestamp=?, date=? WHERE str=? AND nullable=? AND int=? AND float=? AND bool=? AND timestamp=? AND date=?;";
        database.update("test", values, values);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(argument.capture());
        assertEquals(expectedQuery, argument.getValue());

        // string and LocalDate and LocalDateTime
        verify(statement, times(6)).setString(anyInt(), anyString());
        verify(statement, times(2)).setNull(anyInt(), anyInt());
        verify(statement, times(2)).setInt(anyInt(), anyInt());
        verify(statement, times(2)).setBoolean(anyInt(), anyBoolean());
        verify(statement, times(2)).setFloat(anyInt(), anyFloat());
        verify(statement, times(1)).executeUpdate();
    }
}
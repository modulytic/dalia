package com.modulytic.dalia.app.database.include;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface Updateable {
    void add(@NonNull String s) throws SQLException;
    void add(int i) throws SQLException;
    void add(float f) throws SQLException;
    void add(double d) throws SQLException;
    void add(boolean b) throws SQLException;
    void add(@NonNull LocalDateTime dt) throws SQLException;
    void add(@NonNull LocalDate d) throws SQLException;
    void addNull() throws SQLException;
    void add(Object o) throws SQLException;
}

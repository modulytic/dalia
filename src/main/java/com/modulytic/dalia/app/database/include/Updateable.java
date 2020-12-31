package com.modulytic.dalia.app.database.include;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface Updateable {
    void add(String s) throws SQLException;
    void add(int i) throws SQLException;
    void add(float f) throws SQLException;
    void add(double d) throws SQLException;
    void add(boolean b) throws SQLException;
    void add(LocalDateTime dt) throws SQLException;
    void add(LocalDate d) throws SQLException;
    void addNull() throws SQLException;
    void add(Object o) throws SQLException;
}

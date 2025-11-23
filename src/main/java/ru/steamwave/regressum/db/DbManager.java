package ru.steamwave.regressum.db;

import java.sql.Connection;

public class DbManager {

    private final DbConnect db;

    public DbManager() {
        db = new DbConnect();
    }

    public Connection getConnection() {
        return db.getConnection();
    }

    public void close() {
        db.close();
    }
}

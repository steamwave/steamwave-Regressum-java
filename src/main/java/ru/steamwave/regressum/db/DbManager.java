package ru.steamwave.regressum.db;

import java.sql.Connection;

public class DbManager {

    public final DbConnect db;

    public DbManager() {
        // Создаем объект подключения, но пока не открываем его
        this.db = new DbConnect();
    }

    /**
     * Инициализирует подключение к базе данных.
     * Вызывает DbConnect.init(), который проверяет параметры и создает пул.
     */
    public void init() {
        db.init();
    }

    /**
     * Позволяет получить прямой доступ к объекту DbConnect,
     * если это необходимо в Regressum.java.
     */
    public DbConnect getDbConnect() {
        return db;
    }

    public Connection getConnection() {
        return db.getConnection();
    }

    public void close() {
        db.close();
    }
}
package ru.steamwave.regressum.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnect {

    private static final Logger LOGGER = LogManager.getLogger("RegressumDB");
    private HikariDataSource dataSource;

    // Настройки PostgreSQL
    private final String url = "jdbc:postgresql://localhost:5432/regressum?ssl=false";
    private final String user = "regressum_user";
    private final String password = "G380993012158Mazda2911972904199915092009";

    public DbConnect() {
        LOGGER.info("[DbConnect] Инициализация подключения к PostgreSQL...");
        LOGGER.info("[DbConnect] URL: {}", url);
        LOGGER.info("[DbConnect] Пользователь: {}", user);

        try {
            LOGGER.info("[DbConnect] Настройка конфигурации HikariCP...");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            // Настройки пула
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);
            config.setPoolName("RegressumHikariPool");
            config.addDataSourceProperty("socketTimeout", "10");
            config.addDataSourceProperty("tcpKeepAlive", "true");

            LOGGER.info("[DbConnect] Применение конфигурации...");
            dataSource = new HikariDataSource(config);

            LOGGER.info("[DbConnect] Проверка тестового подключения...");
            try (Connection conn = dataSource.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    LOGGER.info("[DbConnect] ✅ Подключение к PostgreSQL успешно установлено!");
                } else {
                    LOGGER.error("[DbConnect] ⚠️ Подключение получено, но оно закрыто!");
                }
            }

        } catch (SQLException e) {
            LOGGER.error("[DbConnect] ❌ SQL ошибка при подключении!", e);
            throw new RuntimeException("Ошибка при тестовом подключении к базе данных", e);
        } catch (Exception e) {
            LOGGER.error("[DbConnect] ❌ Ошибка при создании HikariCP пула!", e);
            throw new RuntimeException("Ошибка при создании HikariCP пула", e);
        }
    }

    /**
     * Возвращает соединение из пула.
     */
    public Connection getConnection() {
        try {
            LOGGER.debug("[DbConnect] Запрос соединения из пула...");
            Connection conn = dataSource.getConnection();

            if (conn != null && !conn.isClosed()) {
                LOGGER.debug("[DbConnect] ✅ Соединение успешно получено из пула!");
            } else {
                LOGGER.warn("[DbConnect] ⚠️ Соединение из пула оказалось закрытым!");
            }

            return conn;
        } catch (SQLException e) {
            LOGGER.error("[DbConnect] ❌ Не удалось получить соединение из пула!", e);
            return null;
        }
    }

    /**
     * Закрывает пул соединений.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("[DbConnect] Закрытие HikariCP пула...");
            dataSource.close();
            LOGGER.info("[DbConnect] ✅ Пул успешно закрыт.");
        } else {
            LOGGER.warn("[DbConnect] ⚠️ Пул уже был закрыт или не инициализирован.");
        }
    }
}

package ru.steamwave.regressum.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.steamwave.regressum.config.RegressumConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnect {

    private static final Logger LOGGER = LogManager.getLogger("RegressumDB");
    private HikariDataSource dataSource;

    public DbConnect() {
        // Пусто, чтобы не спровоцировать IllegalStateException при загрузке мода
    }

    /**
     * Проверяет, заполнены ли обязательные поля в конфиге.
     */
    private boolean validateConfig() {
        try {
            String host = RegressumConfig.DB_HOST.get();
            String user = RegressumConfig.DB_USER.get();
            String dbName = RegressumConfig.DB_NAME.get();

            // Проверяем на пустоту или дефолтные значения (подставь свои, если они другие)
            if (host == null || host.isEmpty() || host.equals("your_default_host")) return false;
            if (user == null || user.isEmpty() || user.equals("your_default_user")) return false;
            if (dbName == null || dbName.isEmpty()) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void init() {
        if (dataSource != null) return;

        // 1. Валидация конфига
        if (!validateConfig()) {
            LOGGER.fatal("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LOGGER.fatal("[DbConnect] ❌ ОШИБКА: Конфиг БД не заполнен!");
            LOGGER.fatal("[DbConnect] Проверьте файл config/regressum-common.toml");
            LOGGER.fatal("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            // Выключаем сервер мгновенно
            Runtime.getRuntime().halt(1);
        }

        try {
            String host = RegressumConfig.DB_HOST.get();
            int port = RegressumConfig.DB_PORT.get();
            String dbName = RegressumConfig.DB_NAME.get();
            String user = RegressumConfig.DB_USER.get();
            String password = RegressumConfig.DB_PASS.get();

            String url = String.format("jdbc:postgresql://%s:%d/%s?ssl=false", host, port, dbName);

            LOGGER.info("[DbConnect] Попытка подключения к PostgreSQL: {}", url);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            // Настройки пула (HikariCP)
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(60000);
            config.setConnectionTimeout(10000); // 10 секунд на попытку
            config.setPoolName("RegressumPool");

            dataSource = new HikariDataSource(config);

            // Тестовое соединение
            try (Connection conn = dataSource.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    LOGGER.info("[DbConnect] ✅ База данных подключена успешно!");
                }
            }

        } catch (Exception e) {
            LOGGER.fatal("[DbConnect] ❌ КРИТИЧЕСКАЯ ОШИБКА при создании пула БД!");
            LOGGER.fatal(e.getMessage());
            // Если пароль неверный или БД выключена — тоже стопаем сервер
            Runtime.getRuntime().halt(1);
        }
    }

    public Connection getConnection() {
        // Если кто-то вызвал до инициализации
        if (dataSource == null) {
            return null;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.error("[DbConnect] ❌ Ошибка получения соединения из пула: {}", e.getMessage());
            return null;
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("[DbConnect] Пул БД закрыт.");
        }
    }
}
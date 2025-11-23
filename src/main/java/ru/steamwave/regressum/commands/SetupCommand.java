package ru.steamwave.regressum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import ru.steamwave.regressum.db.DbManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupCommand {

    private static DbManager dbManager;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DbManager manager) {
        dbManager = manager;

        dispatcher.register(Commands.literal("setup")
                .requires(cs -> cs.hasPermission(2)) // только админы
                .executes(SetupCommand::run)
        );
    }

    private static int run(CommandContext<CommandSourceStack> ctx) {
        if (dbManager == null) {
            ctx.getSource().sendFailure(Component.literal("❌ DbManager не инициализирован!"));
            return 1;
        }

        try (Connection conn = dbManager.getConnection()) {
            if (conn == null) {
                ctx.getSource().sendFailure(Component.literal("❌ Подключение к БД не удалось!"));
                return 1;
            }

            try (Statement stmt = conn.createStatement()) {

                // Таблица игроков
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS players (" +
                                "id SERIAL PRIMARY KEY," +
                                "uuid UUID NOT NULL UNIQUE," +
                                "name TEXT NOT NULL," +
                                "first_seen BIGINT NOT NULL," +
                                "last_seen BIGINT NOT NULL" +
                                ");"
                );

                // Таблица логов блоков
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS block_logs (" +
                                "id SERIAL PRIMARY KEY," +
                                "player_uuid UUID NOT NULL," +          // Соответствует setObject в flushBatch
                                "player_name TEXT NOT NULL," +
                                "block TEXT NOT NULL," +
                                "x INT NOT NULL," +
                                "y INT NOT NULL," +
                                "z INT NOT NULL," +
                                "world TEXT NOT NULL," +
                                "action_time BIGINT NOT NULL," +
                                "action_type TEXT NOT NULL," +
                                "roll_backed BOOLEAN DEFAULT FALSE" +
                                ");"
                );

                // Таблица логов сущностей
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS entity_logs (" +
                                "id SERIAL PRIMARY KEY," +
                                "killer_uuid VARCHAR(36)," +          // Соответствует setObject в flushBatch (может быть null)
                                "killer_name VARCHAR(255)," +
                                "killer_type VARCHAR(255)," +
                                "victim_type VARCHAR(255) NOT NULL," +
                                "victim_name VARCHAR(255) NOT NULL," +
                                "x INT NOT NULL," +
                                "y INT NOT NULL," +
                                "z INT NOT NULL," +
                                "world VARCHAR(255) NOT NULL," +
                                "action_time BIGINT NOT NULL," +
                                "action_type VARCHAR(50) NOT NULL" +
                                ");"
                );

                // Таблица логов предметов
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS item_logs (" +
                                "id SERIAL PRIMARY KEY," +
                                "player_uuid UUID DEFAULT NULL," +                // Может быть null
                                "player_name TEXT NOT NULL," +
                                "item_name TEXT NOT NULL," +
                                "x INT NOT NULL," +
                                "y INT NOT NULL," +
                                "z INT NOT NULL," +
                                "world TEXT NOT NULL," +
                                "action_time BIGINT NOT NULL," +                 // Соответствует item.time
                                "action_type TEXT NOT NULL," +
                                "amount INT NOT NULL," +
                                "roll_backed BOOLEAN DEFAULT FALSE" +
                                ");"
                );
                // Таблица логов контейнеров (container_logs)
                stmt.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS container_logs (" +
                                "id SERIAL PRIMARY KEY," +
                                "player_uuid UUID DEFAULT NULL," +               // Может быть null
                                "player_name TEXT NOT NULL," +
                                "item_name TEXT NOT NULL," +
                                "x INT NOT NULL," +
                                "y INT NOT NULL," +
                                "z INT NOT NULL," +
                                "world TEXT NOT NULL," +
                                "action_time BIGINT NOT NULL," +                 // Единое название с item_logs
                                "action_type TEXT NOT NULL," +                   // 'place', 'take'
                                "amount INT NOT NULL," +                         // Единое название с item_logs
                                "container_type TEXT NOT NULL," +                // Тип контейнера
                                "container_data TEXT," +                         // Дополнительные данные
                                "roll_backed BOOLEAN DEFAULT FALSE" +            // Единое поле с item_logs
                                ");"
                );


                // Индексы для оптимизации
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_block_logs_player_uuid ON block_logs(player_uuid);");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_block_logs_action_time ON block_logs(action_time);");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_entity_logs_killer_uuid ON entity_logs(killer_uuid);");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_entity_logs_action_time ON entity_logs(action_time);");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_item_logs_player_uuid ON item_logs(player_uuid);");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_item_logs_action_time ON item_logs(action_time);");

                ctx.getSource().sendSuccess(() -> Component.literal("✅ Setup завершён: все таблицы готовы!"), true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ctx.getSource().sendFailure(Component.literal("❌ Ошибка при создании таблиц: " + e.getMessage()));
        }

        return 1;
    }
}
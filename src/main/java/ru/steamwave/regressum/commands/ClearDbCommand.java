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

public class ClearDbCommand {

    private static DbManager dbManager;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DbManager manager) {
        dbManager = manager;

        dispatcher.register(Commands.literal("fresh")
                .requires(cs -> cs.hasPermission(2)) // только админы
                .executes(ClearDbCommand::run)
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

                // Очистка таблицы блоков
                stmt.executeUpdate("TRUNCATE TABLE block_logs RESTART IDENTITY CASCADE;");
                // Очистка таблицы сущностей
                stmt.executeUpdate("TRUNCATE TABLE entity_logs RESTART IDENTITY CASCADE;");
                // Очистка таблицы контейнеров
                stmt.executeUpdate("TRUNCATE TABLE item_logs RESTART IDENTITY CASCADE;");


                ctx.getSource().sendSuccess(() -> Component.literal(
                        "✅ Очистка завершена: block_logs, entity_logs, container_logs очищены!"), true);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            ctx.getSource().sendFailure(Component.literal("❌ Ошибка при очистке таблиц: " + e.getMessage()));
        }

        return 1;
    }
}

package ru.steamwave.regressum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import ru.steamwave.regressum.db.DbManager;

public class DbCommands {

    private static DbManager dbManager;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, DbManager manager) {
        dbManager = manager;

        // /dbstatus
        dispatcher.register(
                Commands.literal("dbstatus")
                        .requires(cs -> cs.hasPermission(2)) // Только админ
                        .executes(DbCommands::dbStatus)
        );

        // /dbconnect
        dispatcher.register(
                Commands.literal("dbconnect")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(DbCommands::dbConnect)
        );
    }

    private static int dbStatus(CommandContext<CommandSourceStack> ctx) {
        if (dbManager.getConnection() != null) {
            ctx.getSource().sendSuccess(() -> Component.literal("✅ БД подключена!"), false);
        } else {
            ctx.getSource().sendFailure(Component.literal("❌ БД не подключена!"));
        }
        return 1;
    }

    private static int dbConnect(CommandContext<CommandSourceStack> ctx) {
        try {
            if (dbManager.getConnection() != null) {
                ctx.getSource().sendSuccess(() -> Component.literal("✅ Уже подключено к БД!"), false);
            } else {
                dbManager.getConnection(); // Попробует подключиться
                if (dbManager.getConnection() != null) {
                    ctx.getSource().sendSuccess(() -> Component.literal("✅ Успешно подключились к БД!"), false);
                } else {
                    ctx.getSource().sendFailure(Component.literal("❌ Подключение к БД не удалось!"));
                }
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("❌ Ошибка подключения: " + e.getMessage()));
            e.printStackTrace();
        }
        return 1;
    }
}

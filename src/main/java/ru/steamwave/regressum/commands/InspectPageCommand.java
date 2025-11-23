package ru.steamwave.regressum.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import ru.steamwave.regressum.utils.InspectorUtil;
import ru.steamwave.regressum.utils.InspectorUtil.InspectorState;
import ru.steamwave.regressum.events.InspectorEvents; // Импортируем InspectorEvents

public class InspectPageCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("inspect_page")
                        .then(Commands.literal("next")
                                .executes(ctx -> changePage(ctx.getSource(), 1)))
                        .then(Commands.literal("prev")
                                .executes(ctx -> changePage(ctx.getSource(), -1)))
                        .then(Commands.literal("reset")
                                .executes(ctx -> changePage(ctx.getSource(), 0)))
        );
    }

    private static int changePage(CommandSourceStack source, int delta) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cЭту команду может использовать только игрок."));
            return 0;
        }

        InspectorState state = InspectorUtil.getState(player.getUUID());
        if (!state.enabled) {
            player.sendSystemMessage(Component.literal("§cРежим инспектора не включён!"));
            return 0;
        }

        // Обновляем страницу
        if (delta == 0) {
            state.currentPage = 0;
        } else {
            state.currentPage += delta;
        }

        if (state.currentPage < 0) state.currentPage = 0;

        // Используем статические методы InspectorEvents
        if ("block".equals(state.logType) && state.lastPos != null) {
            InspectorEvents.showBlockHistory(player, state.lastPos, player.level().getBlockState(state.lastPos), state.currentPage);
        } else if ("container".equals(state.logType) && state.lastPos != null) {
            InspectorEvents.showContainerHistory(player, state.lastPos, state.currentPage);
        } else {
            player.sendSystemMessage(Component.literal("§cНет выбранного объекта для просмотра истории."));
        }

        return 1;
    }
}
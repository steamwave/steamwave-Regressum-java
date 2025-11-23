package ru.steamwave.regressum.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ru.steamwave.regressum.events.InspectorEvents; // Импортируем InspectorEvents
import ru.steamwave.regressum.utils.InspectorUtil;
import ru.steamwave.regressum.utils.InspectorUtil.InspectorState;

public class InspectCommand {

    // Убираем экземпляр InspectorEvents

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rg")
                        .then(Commands.literal("inspect")
                                .then(Commands.literal("toggle")
                                        .executes(ctx -> toggleInspector(ctx.getSource()))
                                )
                                .then(Commands.literal("page")
                                        .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setPage(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                                        )
                                )
                        )
        );
    }

    private static int toggleInspector(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Только игрок может включать режим инспектора!"));
            return 0;
        }
        InspectorUtil.toggleInspect(player);
        return 1;
    }

    private static int setPage(CommandSourceStack source, int page) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Только игрок может менять страницу!"));
            return 0;
        }

        InspectorState state = InspectorUtil.getState(player.getUUID());
        if (!state.enabled) {
            player.sendSystemMessage(Component.literal("Режим инспектора выключен! Используй /rg inspect toggle"));
            return 0;
        }

        InspectorUtil.setPage(player, page);

        if (state.lastPos == null) {
            player.sendSystemMessage(Component.literal("Нет выбранного блока или контейнера для просмотра истории!"));
            return 0;
        }

        // Используем статические методы InspectorEvents
        if ("container".equals(state.logType)) {
            InspectorEvents.showContainerHistory(player, state.lastPos, page);
        } else {
            InspectorEvents.showBlockHistory(player, state.lastPos, player.level().getBlockState(state.lastPos), page);
        }

        return 1;
    }
}
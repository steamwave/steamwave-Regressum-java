package ru.steamwave.regressum.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InspectorUtil {

    private static final Map<UUID, InspectorState> inspectors = new HashMap<>();

    public static class InspectorState {
        public boolean enabled = false;
        public int currentPage = 0;
        public String logType; // "block" или "container"
        public BlockPos lastPos; // последний выбранный блок/контейнер
    }

    public static void toggleInspect(ServerPlayer player) {
        UUID uuid = player.getUUID();
        InspectorState state = inspectors.computeIfAbsent(uuid, k -> new InspectorState());
        state.enabled = !state.enabled;
        state.currentPage = 0;
        player.sendSystemMessage(Component.literal(state.enabled ? "§aРежим инспектора включён" : "§cРежим инспектора выключен"));
    }

    public static boolean isInspecting(UUID uuid) {
        return inspectors.containsKey(uuid) && inspectors.get(uuid).enabled;
    }

    public static InspectorState getState(UUID uuid) {
        return inspectors.computeIfAbsent(uuid, k -> new InspectorState());
    }

    public static InspectorState setPage(ServerPlayer player, int page) {
        InspectorState state = getState(player.getUUID());
        state.currentPage = page;
        return state;
    }
}

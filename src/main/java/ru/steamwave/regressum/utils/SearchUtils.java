package ru.steamwave.regressum.utils;

import ru.steamwave.regressum.storage.CacheManager;
import ru.steamwave.regressum.storage.BlockLogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SearchUtils {

    /**
     * Поиск последних действий на конкретной позиции
     */
    public static List<BlockLogEntry> searchByPosition(String world, int x, int y, int z, int limit) {
        return CacheManager.getInstance().getHistoryAtPos(world, x, y, z, limit);
    }

    /**
     * Поиск последних действий игрока
     */
    public static List<BlockLogEntry> searchByPlayer(UUID playerUuid, int limit) {
        return CacheManager.getInstance().getRecentByPlayer(playerUuid, limit);
    }

    /**
     * Фильтр по типу блока
     */
    public static List<BlockLogEntry> filterByBlockType(List<BlockLogEntry> logs, String blockType) {
        List<BlockLogEntry> result = new ArrayList<>();
        for (BlockLogEntry log : logs) {
            if (log.blockType.equalsIgnoreCase(blockType)) {
                result.add(log);
            }
        }
        return result;
    }

    /**
     * Фильтр по откатанным действиям
     */
    public static List<BlockLogEntry> filterRolledBack(List<BlockLogEntry> logs, boolean rolledBack) {
        List<BlockLogEntry> result = new ArrayList<>();
        for (BlockLogEntry log : logs) {
            if (log.rolledBack == rolledBack) result.add(log);
        }
        return result;
    }
}

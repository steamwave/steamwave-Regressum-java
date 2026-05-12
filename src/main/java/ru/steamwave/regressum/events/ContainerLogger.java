package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import ru.steamwave.regressum.utils.RegressumContext;

public class ContainerLogger {

    public static void log(IItemHandler handler, ItemStack stack, int amount, String action, BlockPos pos) {
        try {
            // Пытаемся взять игрока, но не падаем, если не вышло
            String playerName = "System/Machine";
            try {
                Player player = RegressumContext.getCurrentPlayer();
                if (player != null) playerName = player.getScoreboardName();
            } catch (Exception ignored) {}

            String posStr = (pos != null) ? pos.getX() + "," + pos.getY() + "," + pos.getZ() : "Unknown";

            // Используем встроенный метод получения имени, чтобы не дергать сложные hover-тексты
            String itemName = stack != null ? stack.getItem().toString() : "Empty";

            // Прямой вывод без format (на случай ошибок в строке формата)
            System.out.println(">>> [REGRESSUM] " + playerName + " | Action: " + action + " | Item: " + itemName + " x" + amount + " | Pos: " + posStr);

        } catch (Exception e) {
            System.err.println("[REGRESSUM ERROR] Ошибка при попытке залогировать действие!");
            e.printStackTrace();
        }
    }
}
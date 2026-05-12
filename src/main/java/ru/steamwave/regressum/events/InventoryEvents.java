package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryEvents {
    // Карта: Координаты блока -> UUID последнего игрока, кто кликнул
    private static final Map<BlockPos, UUID> lastInteraction = new HashMap<>();

    // Запоминаем игрока при клике
    public static void recordInteraction(BlockPos pos, UUID player) {
        lastInteraction.put(pos, player);
    }

    public static void logInsert(BlockPos pos, int slot, ItemStack stack) {
        UUID playerUUID = lastInteraction.get(pos);
        if (playerUUID == null) return;

        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            // Достаем ник игрока
            String playerName = player.getScoreboardName();
            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            int count = stack.getCount();

            // Добавляем %s в начало строки для имени
            player.sendSystemMessage(Component.literal(
                    String.format("§6%s §a[+]§f Положил §e%dx %s§f в слот §7%d§f (@ %d, %d, %d)",
                            playerName, count, itemName, slot, pos.getX(), pos.getY(), pos.getZ())
            ));
        }
    }

    public static void logExtract(BlockPos pos, int slot, ItemStack stack) {
        UUID playerUUID = lastInteraction.get(pos);
        if (playerUUID == null) return;

        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            // Достаем ник игрока
            String playerName = player.getScoreboardName();
            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            int count = stack.getCount();

            // Добавляем %s в начало строки для имени
            player.sendSystemMessage(Component.literal(
                    String.format("§6%s §c[-]§f Забрал §e%dx %s§f из слота §7%d§f (@ %d, %d, %d)",
                            playerName, count, itemName, slot, pos.getX(), pos.getY(), pos.getZ())
            ));
        }
    }
}
package ru.steamwave.regressum.utils;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.Map;
import java.util.WeakHashMap;

public class InventoryContext {
    // Выносим карту памяти сюда
    private static final Map<IItemHandler, BlockPos> posMap = new WeakHashMap<>();

    public static void setPos(IItemHandler handler, BlockPos pos) {
        posMap.put(handler, pos);
    }

    public static BlockPos getPos(IItemHandler handler) {
        return posMap.get(handler);
    }
}
package ru.steamwave.regressum.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IInternalInventory {
    void regressum$setContext(BlockPos pos, Level level);
    BlockPos regressum$getPos();
    Level regressum$getLevel();
}
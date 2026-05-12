package ru.steamwave.regressum.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.wrapper.LoggingItemHandler;

@Mixin(BlockCapability.class)
public class BlockCapabilityMixin {

    @Inject(
            method = "getCapability",
            at = @At("RETURN"),
            cancellable = true,
            remap = false // Это важно, так как это код NeoForge
    )
    private void wrapInventory(
            Level level,
            BlockPos pos,
            @Nullable BlockState state,
            @Nullable BlockEntity blockEntity,
            Object context, // Context в исходнике - это C, в миксине пишем Object
            CallbackInfoReturnable<Object> cir
    ) {
        // Проверяем: это запрос инвентаря?
        if ((Object)this == Capabilities.ItemHandler.BLOCK) {
            Object result = cir.getReturnValue();

            // Если инвентарь нашелся и мы его еще не оборачивали
            if (result instanceof IItemHandler handler && !(handler instanceof LoggingItemHandler)) {

                // Подменяем результат на нашу обертку
                // Мы передаем pos, чтобы логгер знал, где стоит сундук
                cir.setReturnValue(new LoggingItemHandler(handler, pos));
            }
        }
    }
}
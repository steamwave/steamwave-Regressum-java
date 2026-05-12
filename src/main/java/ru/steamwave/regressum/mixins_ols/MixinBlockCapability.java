package ru.steamwave.regressum.mixins_ols;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.utils.IInternalInventory;

@Mixin(value = BlockCapability.class, remap = false)
public class MixinBlockCapability {

    @Inject(method = "getCapability", at = @At("RETURN"), remap = false)
    private void onGetCapability(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Object context, CallbackInfoReturnable<Object> cir) {
        Object result = cir.getReturnValue();

        // Если это наш меченый инвентарь и мы на сервере
        if (result instanceof IInternalInventory internal && !level.isClientSide) {
            // Прошиваем координаты прямо из аргументов метода!
            internal.regressum$setContext(pos, level);
        }
    }
}
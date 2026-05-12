package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import ru.steamwave.regressum.utils.IInternalInventory;

@Mixin(BlockEntity.class)
public class MixinBlockEntity {

    @Inject(method = "getCapability", at = @At("RETURN"), remap = false)
    private void onGetCapability(net.neoforged.neoforge.capabilities.BlockCapability<?, ?> cap,
                                 net.minecraft.core.Direction side,
                                 org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Object> cir) {

        // Проверяем, что запрашивают именно инвентарь
        if (cap == Capabilities.ItemHandler.BLOCK) {
            Object result = cir.getReturnValue();
            if (result instanceof IInternalInventory internal) {
                BlockEntity be = (BlockEntity) (Object) this;
                if (be.getLevel() != null) {
                    internal.regressum$setContext(be.getBlockPos(), be.getLevel());
                }
            }
        }
    }
}
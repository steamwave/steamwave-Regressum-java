package ru.steamwave.regressum.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.WrappedItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.events.InventoryEvents;

@Mixin(targets = "net.neoforged.neoforge.capabilities.BlockCapability$IBlockCapabilityProvider")
public abstract class BlockCapabilityMixin {

    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetCapability(Object container, Object context, CallbackInfoReturnable<Object> cir) {
        Object result = cir.getReturnValue();

        if (result instanceof IItemHandler handler && !(handler instanceof InventoryEvents)) {
            BlockPos pos = null;
            if (container instanceof BlockEntity be) {
                pos = be.getBlockPos();
            } else if (context instanceof BlockPos p) {
                pos = p;
            }

            if (pos != null) {
                IItemHandler actual = handler;
                // Разворачиваем вложенные обертки (SidedInvWrapper и т.д.)
                while (actual instanceof WrappedItemHandler wrapped) {
                    actual = wrapped.getHandler();
                }
                cir.setReturnValue(new InventoryEvents(actual, pos));
            }
        }
    }
}
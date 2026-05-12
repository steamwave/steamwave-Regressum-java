package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.events.ContainerLogger;

@Pseudo
@Mixin(targets = "com.simibubi.create.foundation.item.ItemHandlerWrapper", remap = false)
public abstract class MixinItemHandlerWrapper {

    @Shadow
    protected IItemHandlerModifiable wrapped;

    @Inject(
            method = "insertItem",
            at = @At("RETURN"),
            remap = false,
            require = 0
    )
    private void onInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate && !stack.isEmpty()) {
            ItemStack remainder = cir.getReturnValue();
            int inserted = stack.getCount() - (remainder.isEmpty() ? 0 : remainder.getCount());

            if (inserted > 0) {
                // Пытаемся достать позицию из вложенного обработчика
                ContainerLogger.log(this.wrapped, stack, inserted, "CREATE_INSERT", null);
            }
        }
    }


    @Inject(
            method = "extractItem",
            at = @At("RETURN"),
            remap = false,
            require = 0
    )
    private void onExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate) {
            ItemStack result = cir.getReturnValue();
            if (!result.isEmpty()) {
                ContainerLogger.log(this.wrapped, result, result.getCount(), "CREATE_EXTRACT", null);
            }
        }
    }
}
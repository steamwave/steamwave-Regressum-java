package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.events.ContainerLogger;

@Pseudo
@Mixin(targets = "com.simibubi.create.foundation.item.DirectInventory", remap = false)
public abstract class MixinDirectInventory {

    @Shadow
    @Final
    private BlockEntity blockEntity;

    // Ловим момент, когда предмет вставляется в слот (Депо, Конвейер и т.д.)
    @Inject(method = "setStackInSlot", at = @At("HEAD"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (!stack.isEmpty() && blockEntity != null) {
            ContainerLogger.log(null, stack, stack.getCount(), "CREATE_INSERT", blockEntity.getBlockPos());
        }
    }

    // Ловим момент извлечения
    @Inject(method = "extractItem", at = @At("RETURN"))
    private void onExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!simulate && !result.isEmpty() && blockEntity != null) {
            ContainerLogger.log(null, result, result.getCount(), "CREATE_EXTRACT", blockEntity.getBlockPos());
        }
    }
}
package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.events.ContainerLogger;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

@Pseudo
@Mixin(value = InvManipulationBehaviour.class, remap = false)
public abstract class MixinInvManipulationBehaviour {

    @Inject(method = "insert", at = @At("RETURN"))
    private void onInsert(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack remainder = cir.getReturnValue();
        int inserted = stack.getCount() - remainder.getCount();

        if (inserted > 0) {
            // Используем прямой каст к классу, который ты нашел
            // Это "this", потому что InvManipulationBehaviour является наследником BlockEntityBehaviour
            BlockEntityBehaviour behavior = (BlockEntityBehaviour)(Object)this;
            if (behavior.blockEntity != null) {
                ContainerLogger.log(null, stack, inserted, "PLACE", behavior.blockEntity.getBlockPos());
            }
        }
    }

    @Inject(method = "extract()Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void onExtract(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            BlockEntityBehaviour behavior = (BlockEntityBehaviour)(Object)this;
            if (behavior.blockEntity != null) {
                ContainerLogger.log(null, result, result.getCount(), "TAKE", behavior.blockEntity.getBlockPos());
            }
        }
    }
}
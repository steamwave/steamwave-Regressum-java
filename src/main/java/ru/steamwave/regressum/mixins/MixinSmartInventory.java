package ru.steamwave.regressum.mixins;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.utils.RegressumContext;


@Mixin(targets = "com.simibubi.create.foundation.item.SmartInventory", remap = false)
public class MixinSmartInventory {

    @Inject(method = "insertItem", at = @At("HEAD"))
    private void onInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate) {
            String playerName = RegressumContext.getCurrentPlayer() != null ?
                    RegressumContext.getCurrentPlayer().getName().getString() : "Unknown/Mechanism";

            System.out.println("[Regressum] SmartInventory INSERT | Игрок: " + playerName +
                    " | Предмет: " + stack.getItem() +
                    " | Позиция из контекста: " + RegressumContext.getPos());
        }
    }

    @Inject(method = "extractItem", at = @At("HEAD"))
    private void onExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate) {
            String playerName = RegressumContext.getCurrentPlayer() != null ?
                    RegressumContext.getCurrentPlayer().getName().getString() : "Unknown/Mechanism";

            System.out.println("[Regressum] SmartInventory EXTRACT | Игрок: " + playerName +
                    " | Кол-во: " + amount +
                    " | Позиция: " + RegressumContext.getPos());
        }
    }
}
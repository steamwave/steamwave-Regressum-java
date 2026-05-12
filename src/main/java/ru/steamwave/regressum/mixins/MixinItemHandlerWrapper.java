package ru.steamwave.regressum.mixins;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Используем строковый путь, чтобы не зависеть от импортов при загрузке
@Mixin(targets = "com.simibubi.create.foundation.item.ItemHandlerWrapper", remap = false)
public class MixinItemHandlerWrapper {

    @Inject(method = "insertItem", at = @At("HEAD"))
    private void debugInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        // Печатаем ВООБЩЕ всё, даже симуляции, чтобы понять, что миксин жив
        System.out.println("[Regressum CRITICAL] ВЫЗВАН INSERT! Предмет: " + (stack != null ? stack.getItem() : "null"));
    }

    @Inject(method = "extractItem", at = @At("HEAD"))
    private void debugExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        System.out.println("[Regressum CRITICAL] ВЫЗВАН EXTRACT! Слот: " + slot + " Кол-во: " + amount);
    }
}
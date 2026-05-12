package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.steamwave.regressum.events.ContainerLogger;

@Mixin(Slot.class)
public abstract class MixinSlot {
    @Shadow public abstract ItemStack getItem();

    private ItemStack stackBefore = ItemStack.EMPTY;

    @Inject(method = "set", at = @At("HEAD"))
    private void onSetBefore(ItemStack stack, CallbackInfo ci) {
        // Запоминаем, что было в слоте до изменения
        this.stackBefore = this.getItem().copy();
    }

    @Inject(method = "set", at = @At("RETURN"))
    private void onSetAfter(ItemStack stack, CallbackInfo ci) {
        ItemStack stackAfter = this.getItem();

        if (ItemStack.matches(stackBefore, stackAfter)) return;

        // Определяем позицию блока
        net.minecraft.core.BlockPos pos = null;
        // Почти у всех слотов есть ссылка на container (инвентарь)
        Slot slot = (Slot) (Object) this;

        if (slot.container instanceof net.minecraft.world.level.block.entity.BlockEntity be) {
            pos = be.getBlockPos();
        } else if (slot.container instanceof net.minecraft.world.Nameable nameable) {
            // Если это не BlockEntity напрямую, иногда можно попробовать дотянуться иначе,
            // но для ванили BE достаточно.
        }

        if (stackAfter.getCount() > stackBefore.getCount()) {
            int diff = stackAfter.getCount() - stackBefore.getCount();
            ContainerLogger.log(null, stackAfter, diff, "PLACE", pos);
        } else if (stackAfter.getCount() < stackBefore.getCount()) {
            int diff = stackBefore.getCount() - stackAfter.getCount();
            ContainerLogger.log(null, stackBefore, diff, "TAKE", pos);
        }
    }
}
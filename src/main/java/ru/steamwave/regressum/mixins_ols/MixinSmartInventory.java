package ru.steamwave.regressum.mixins_ols;

import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.blockEntity.ItemHandlerContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.events.ContainerLogger;
import java.lang.reflect.Field;

@Pseudo
@Mixin(value = SmartInventory.class, remap = false)
public abstract class MixinSmartInventory extends ItemHandlerContainer {

    // Пустой конструктор, чтобы Java не ругалась на наследование
    public MixinSmartInventory() { super(null); }

    @Inject(method = "insertItem", at = @At("RETURN"))
    private void onInsert(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate && !stack.isEmpty()) {
            ItemStack remainder = cir.getReturnValue();
            int inserted = stack.getCount() - remainder.getCount();
            if (inserted > 0) {
                logSmartAction(stack, inserted, "CREATE_INSERT");
            }
        }
    }

    @Inject(method = "extractItem", at = @At("RETURN"))
    private void onExtract(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (!simulate) {
            ItemStack result = cir.getReturnValue();
            if (!result.isEmpty()) {
                logSmartAction(result, result.getCount(), "CREATE_EXTRACT");
            }
        }
    }

    private void logSmartAction(ItemStack stack, int amount, String action) {
        BlockPos pos = BlockPos.ZERO;
        try {
            // Магия рефлексии, чтобы достать приватное поле blockEntity из SyncedStackHandler
            // так как оно находится во внутреннем защищенном классе
            Object handler = this.inv;
            Field beField = handler.getClass().getDeclaredField("blockEntity");
            beField.setAccessible(true);
            Object be = beField.get(handler);

            if (be instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
                pos = blockEntity.getBlockPos();
            }
        } catch (Exception e) {
            // Если не вышло достать координаты, просто пишем в лог с 0,0,0 для теста
        }

        ContainerLogger.log(null, stack, amount, action, pos);
    }
}
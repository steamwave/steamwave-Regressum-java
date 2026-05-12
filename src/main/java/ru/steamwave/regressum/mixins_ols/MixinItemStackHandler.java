package ru.steamwave.regressum.mixins_ols;

import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import ru.steamwave.regressum.utils.IInternalInventory;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class MixinItemStackHandler implements IInternalInventory {

    @Unique
    private BlockPos regressum$pos;
    @Unique
    private Level regressum$level;

    @Override
    public void regressum$setContext(BlockPos pos, Level level) {
        this.regressum$pos = pos;
        this.regressum$level = level;
    }

    @Override
    public BlockPos regressum$getPos() {
        return this.regressum$pos;
    }

    @org.spongepowered.asm.mixin.injection.Inject(
            method = "insertItem",
            at = @At("RETURN"),
            remap = false
    )
    private void onInsert(int slot, net.minecraft.world.item.ItemStack stack, boolean simulate, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.item.ItemStack> cir) {
        if (simulate || stack.isEmpty()) return;

        int inserted = stack.getCount() - cir.getReturnValue().getCount();

        if (inserted > 0) {
            BlockPos pos = this.regressum$getPos();
            ru.steamwave.regressum.events.ContainerLogger.log((net.neoforged.neoforge.items.IItemHandler)this, stack, inserted, "INSERT", pos);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(
            method = "extractItem",
            at = @At("RETURN"),
            remap = false
    )
    private void onExtract(int slot, int amount, boolean simulate, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.item.ItemStack> cir) {
        if (simulate) return;

        net.minecraft.world.item.ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            BlockPos pos = this.regressum$getPos();
            ru.steamwave.regressum.events.ContainerLogger.log((net.neoforged.neoforge.items.IItemHandler)this, result, result.getCount(), "EXTRACT", pos);
        }
    }
}
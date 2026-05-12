package ru.steamwave.regressum.wrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import ru.steamwave.regressum.events.InventoryEvents;

import java.util.UUID;

public class LoggingItemHandler implements IItemHandler {
    private final IItemHandler wrapped;
    private final BlockPos pos;

    public LoggingItemHandler(IItemHandler wrapped, BlockPos pos) {
        this.wrapped = wrapped;
        this.pos = pos;
    }

    @Override
    public int getSlots() {
        return wrapped.getSlots();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return wrapped.getStackInSlot(slot);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack result = wrapped.insertItem(slot, stack, simulate);

        // Логируем только реальные изменения (не симуляции)
        if (!simulate && !ItemStack.matches(stack, result)) {
            int inserted = stack.getCount() - result.getCount();
            if (inserted > 0) {
                ItemStack actualInserted = stack.copy();
                actualInserted.setCount(inserted);
                InventoryEvents.logInsert(pos, slot, actualInserted);
            }
        }

        return result;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = wrapped.extractItem(slot, amount, simulate);

        // Логируем только реальные извлечения
        if (!simulate && !result.isEmpty()) {
            InventoryEvents.logExtract(pos, slot, result);
        }

        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return wrapped.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return wrapped.isItemValid(slot, stack);
    }

    // Важно для проверки рекурсии
    public IItemHandler unwrap() {
        return wrapped;
    }
}
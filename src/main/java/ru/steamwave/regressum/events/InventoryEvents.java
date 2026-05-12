package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InventoryEvents implements IItemHandler {
    private static final Logger LOGGER = LogManager.getLogger("Regressum-Log");
    private final IItemHandler delegate;
    private final BlockPos pos;

    public InventoryEvents(IItemHandler delegate, BlockPos pos) {
        this.delegate = delegate;
        this.pos = pos;
    }

    @Override
    public int getSlots() { return delegate.getSlots(); }
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) { return delegate.getStackInSlot(slot); }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack result = delegate.insertItem(slot, stack, simulate);
        if (!simulate) {
            int diff = stack.getCount() - result.getCount();
            if (diff > 0) log("ПОЛОЖИЛИ", stack, diff);
        }
        return result;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = delegate.extractItem(slot, amount, simulate);
        if (!simulate && !result.isEmpty()) {
            log("ЗАБРАЛИ", result, result.getCount());
        }
        return result;
    }

    private void log(String action, ItemStack stack, int count) {
        String actor = findActor().map(ServerPlayer::getScoreboardName).orElse("Система/Труба");
        String msg = String.format("%s: %dx %s в [%d, %d, %d] (%s)",
                action, count, stack.getHoverName().getString(), pos.getX(), pos.getY(), pos.getZ(), actor);

        LOGGER.info(msg);
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§e[Regressum] §f" + msg), false);
        }
    }

    private Optional<ServerPlayer> findActor() {
        if (ServerLifecycleHooks.getCurrentServer() == null) return Optional.empty();
        // Ищем игрока в радиусе 8 блоков, у которого открыт любой контейнер
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream()
                .filter(p -> p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64)
                .findFirst();
    }

    @Override public int getSlotLimit(int slot) { return delegate.getSlotLimit(slot); }
    @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return delegate.isItemValid(slot, stack); }
}
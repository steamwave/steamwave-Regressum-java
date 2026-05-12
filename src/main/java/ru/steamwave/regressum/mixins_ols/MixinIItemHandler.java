package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.steamwave.regressum.utils.IInternalInventory;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

// Важно: remap = false, так как это интерфейс NeoForge
@Mixin(value = IItemHandler.class, remap = false)
public interface MixinIItemHandler {

    @Inject(method = "insertItem", at = @At("RETURN"), remap = false)
    default void onInsertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        // Если это была просто проверка (simulate) или стак пустой — выходим
        if (simulate || stack.isEmpty()) return;

        // Вычисляем, сколько реально вошло в слот
        ItemStack remainder = cir.getReturnValue();
        int inserted = stack.getCount() - remainder.getCount();

        if (inserted > 0) {
            String context = "Unknown Location";

            // Используем твой интерфейс для получения координат
            if (this instanceof IInternalInventory internal) {
                var pos = internal.regressum$getPos(); // Предполагаю, у тебя есть геттер
                if (pos != null) {
                    context = pos.toShortString();
                }
            }

            sendDebugMessage("§a[Regressum]§f Вставлено: " + inserted + "x " + stack.getItem().toString() + " @ " + context);
        }
    }

    @Inject(method = "extractItem", at = @At("RETURN"), remap = false)
    default void onExtractItem(int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (simulate) return;

        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty()) {
            String context = "Unknown Location";

            if (this instanceof IInternalInventory internal) {
                var pos = internal.regressum$getPos();
                if (pos != null) {
                    context = pos.toShortString();
                }
            }

            sendDebugMessage("§c[Regressum]§f Извлечено: " + result.getCount() + "x " + result.getItem().toString() + " @ " + context);
        }
    }

    // Временный метод для теста в чате
    private static void sendDebugMessage(String text) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(text), false);
        }
    }
}
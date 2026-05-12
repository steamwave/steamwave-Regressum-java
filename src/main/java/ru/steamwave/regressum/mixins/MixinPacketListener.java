package ru.steamwave.regressum.mixins;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinPacketListener {

    @Shadow public ServerPlayer player;

    @Unique
    private ItemStack regressum$itemBeforeClick = ItemStack.EMPTY;
    @Unique
    private int regressum$lastSlotId = -1;

    @Inject(method = "handleContainerClick", at = @At("HEAD"))
    private void onBeforeClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        AbstractContainerMenu menu = player.containerMenu;

        if (menu != null && menu.containerId == packet.getContainerId()) {
            int slotNum = packet.getSlotNum();

            if (slotNum >= 0 && slotNum < menu.slots.size()) {
                // Запоминаем состояние слота до клика
                this.regressum$itemBeforeClick = menu.getSlot(slotNum).getItem().copy();
                this.regressum$lastSlotId = slotNum;
            }
        }
    }

    @Inject(method = "handleContainerClick", at = @At("RETURN"))
    private void onAfterClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        AbstractContainerMenu menu = player.containerMenu;

        // Проверяем, что мы запомнили слот и меню все еще то же самое
        if (menu != null && menu.containerId == packet.getContainerId() && regressum$lastSlotId != -1) {
            Slot slot = menu.getSlot(regressum$lastSlotId);
            ItemStack itemAfter = slot.getItem();

            // Сравниваем: изменилось ли содержимое слота
            if (!ItemStack.matches(regressum$itemBeforeClick, itemAfter)) {

                // Логика определения действия
                String action = "UPDATE";
                int amount = 0;

                if (regressum$itemBeforeClick.isEmpty() && !itemAfter.isEmpty()) {
                    action = "INSERT";
                    amount = itemAfter.getCount();
                } else if (!regressum$itemBeforeClick.isEmpty() && itemAfter.isEmpty()) {
                    action = "EXTRACT";
                    amount = regressum$itemBeforeClick.getCount();
                } else {
                    // Если изменилось количество или тип предмета
                    amount = Math.abs(itemAfter.getCount() - regressum$itemBeforeClick.getCount());
                }

                // ВЫВОД В ЛОГ
                System.out.println(String.format(
                        "[Regressum LOG] Игрок: %s | Действие: %s | Предмет: %s | Кол-во: %d | Слот: %d",
                        player.getName().getString(),
                        action,
                        itemAfter.isEmpty() ? regressum$itemBeforeClick.getItem() : itemAfter.getItem(),
                        amount,
                        regressum$lastSlotId
                ));
            }
        }

        // Сбрасываем временные данные
        this.regressum$itemBeforeClick = ItemStack.EMPTY;
        this.regressum$lastSlotId = -1;
    }
}
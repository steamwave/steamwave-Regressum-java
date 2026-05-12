package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = "regressum")
public class OnRightClick {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Логируем только на сервере
        if (event.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = event.getPos();
        UUID playerUUID = event.getEntity().getUUID();

        // Получаем инвентарь блока через капабилити
        IItemHandler handler = event.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);

        if (handler != null) {
            // 1. Делаем снимок инвентаря ДО того, как игрок что-то изменил
            List<ItemStack> before = snapshot(handler);

            // 2. Запоминаем, кто взаимодействует (для твоего старого логгера)
            InventoryEvents.recordInteraction(pos, playerUUID);

            // 3. Планируем проверку через 1 тик.
            // К этому моменту метод клика у Create или сундука уже завершится
            event.getEntity().getServer().tell(new net.minecraft.server.TickTask(0, () -> {
                IItemHandler postHandler = event.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                if (postHandler != null) {
                    // Делаем снимок ПОСЛЕ
                    List<ItemStack> after = snapshot(postHandler);
                    // Сравниваем ДО и ПОСЛЕ
                    compareAndLog(playerUUID, pos, before, after);
                }
            }));
        }
    }

    // Вспомогательный метод для создания снимка всех слотов
    private static List<ItemStack> snapshot(IItemHandler handler) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            // Обязательно .copy(), иначе будем хранить ссылку на меняющийся предмет
            list.add(handler.getStackInSlot(i).copy());
        }
        return list;
    }

    // Сравнение двух списков предметов и вызов лога
    private static void compareAndLog(UUID player, BlockPos pos, List<ItemStack> before, List<ItemStack> after) {
        int slots = Math.min(before.size(), after.size());
        for (int i = 0; i < slots; i++) {
            ItemStack stackBefore = before.get(i);
            ItemStack stackAfter = after.get(i);

            if (!ItemStack.matches(stackBefore, stackAfter)) {
                if (stackAfter.getCount() > stackBefore.getCount()) {
                    // Положили (или заменили на другой предмет)
                    ItemStack added = stackAfter.copy();
                    // Если предмет сменился полностью, считаем весь стак новым
                    int count = ItemStack.isSameItem(stackBefore, stackAfter)
                            ? stackAfter.getCount() - stackBefore.getCount()
                            : stackAfter.getCount();
                    added.setCount(count);
                    InventoryEvents.logInsert(pos, i, added);
                }
                else if (stackBefore.getCount() > stackAfter.getCount()) {
                    // Забрали
                    ItemStack removed = stackBefore.copy();
                    int count = ItemStack.isSameItem(stackBefore, stackAfter)
                            ? stackBefore.getCount() - stackAfter.getCount()
                            : stackBefore.getCount();
                    removed.setCount(count);
                    InventoryEvents.logExtract(pos, i, removed);
                }
            }
        }
    }
}
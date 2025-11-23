package ru.steamwave.regressum.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import ru.steamwave.regressum.network.ContainerActionPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Клиентский трекер для отслеживания изменений в контейнерах с ItemHandler
 */
public class ClientContainerTracker {

    // 🔴 Храним предыдущие состояния контейнеров
    private final Map<BlockPos, ContainerState> containerStates = new HashMap<>();

    /**
     * Обрабатывает взаимодействие с блоком и возвращает пакет с изменениями
     * @return ContainerActionPacket с изменениями или null если изменений нет
     */
    public ContainerActionPacket processContainerInteraction(Level level, BlockPos pos, Player player) {
        // 🔴 Проверяем, есть ли у блока IItemHandler
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null) {
            System.out.println("No IItemHandler found at " + pos);
            return null;
        }

        // 🔴 Получаем текущее состояние контейнера
        List<ItemStack> currentState = getContainerState(handler);
        long currentTime = System.currentTimeMillis();

        // 🔴 Проверяем предыдущее состояние
        ContainerState previousState = containerStates.get(pos);

        ContainerActionPacket packet = null;

        if (previousState != null) {
            // 🔴 Вычисляем diff между состояниями
            List<ContainerActionPacket.SlotChange> changes = calculateDiff(
                    previousState.slots(),
                    currentState,
                    previousState.timestamp(),
                    currentTime
            );

            if (!changes.isEmpty()) {
                // 🔴 Создаем пакет с изменениями
                packet = new ContainerActionPacket(pos, changes, currentTime);
                System.out.println("📤 Created packet with " + changes.size() + " changes for " + pos);

                // 🔴 Логируем изменения для отладки
                for (ContainerActionPacket.SlotChange change : changes) {
                    System.out.println("  └ " + change.actionType() + " " +
                            change.stack().getCount() + "x " +
                            change.stack().getItem().toString() + " in slot " + change.slotIndex());
                }
            } else {
                System.out.println("No changes detected at " + pos);
            }
        } else {
            System.out.println("First interaction with container at " + pos + ", storing initial state");
        }

        // 🔴 Обновляем состояние контейнера (даже если изменений нет)
        containerStates.put(pos, new ContainerState(currentState, currentTime));

        return packet;
    }

    /**
     * Получает полное состояние контейнера
     */
    private List<ItemStack> getContainerState(IItemHandler handler) {
        List<ItemStack> state = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            state.add(stack.copy()); // 🔴 Важно: создаем копию!
        }
        return state;
    }

    /**
     * Вычисляет разницу между двумя состояниями контейнера
     */
    private List<ContainerActionPacket.SlotChange> calculateDiff(
            List<ItemStack> before,
            List<ItemStack> after,
            long beforeTime,
            long afterTime) {

        List<ContainerActionPacket.SlotChange> changes = new ArrayList<>();

        // 🔴 Проверяем все слоты на изменения
        int maxSlots = Math.max(before.size(), after.size());

        for (int slot = 0; slot < maxSlots; slot++) {
            ItemStack beforeStack = slot < before.size() ? before.get(slot) : ItemStack.EMPTY;
            ItemStack afterStack = slot < after.size() ? after.get(slot) : ItemStack.EMPTY;

            if (!ItemStack.matches(beforeStack, afterStack)) {
                String actionType = determineActionType(beforeStack, afterStack);
                ItemStack logStack = createLogStack(beforeStack, afterStack, actionType);

                if (!logStack.isEmpty()) {
                    changes.add(new ContainerActionPacket.SlotChange(
                            slot,
                            logStack,
                            actionType,
                            beforeTime,
                            afterTime
                    ));
                }
            }
        }

        return changes;
    }

    /**
     * Определяет тип действия на основе изменений в слоте
     */
    private String determineActionType(ItemStack before, ItemStack after) {
        // 🔴 Случай 1: Полностью новый предмет
        if (before.isEmpty() && !after.isEmpty()) {
            return "place";
        }

        // 🔴 Случай 2: Предмет полностью изъят
        if (!before.isEmpty() && after.isEmpty()) {
            return "take";
        }

        // 🔴 Случай 3: Изменение количества одинаковых предметов
        if (ItemStack.isSameItemSameComponents(before, after)) {
            if (after.getCount() > before.getCount()) {
                return "place"; // Добавлены предметы
            } else if (after.getCount() < before.getCount()) {
                return "take";  // Изъяты предметы
            }
        }

        // 🔴 Случай 4: Замена одного предмета на другой
        if (!before.isEmpty() && !after.isEmpty() &&
                !ItemStack.isSameItemSameComponents(before, after)) {
            return "swap";
        }

        return "unknown";
    }

    /**
     * Создает ItemStack для логирования (только измененная часть)
     */
    private ItemStack createLogStack(ItemStack before, ItemStack after, String actionType) {
        return switch (actionType) {
            case "place" -> {
                if (before.isEmpty()) {
                    // 🔴 Полностью новый предмет
                    yield after.copy();
                } else {
                    // 🔴 Добавление к существующему предмету
                    ItemStack stack = after.copy();
                    stack.setCount(after.getCount() - before.getCount());
                    yield stack;
                }
            }
            case "take" -> {
                if (after.isEmpty()) {
                    // 🔴 Полностью изъятый предмет
                    yield before.copy();
                } else {
                    // 🔴 Изъятие из существующего предмета
                    ItemStack stack = before.copy();
                    stack.setCount(before.getCount() - after.getCount());
                    yield stack;
                }
            }
            case "swap" -> {
                // 🔴 Для замены логируем новый предмет
                yield after.copy();
            }
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Очищает состояние для конкретного контейнера
     */
    public void clearContainerState(BlockPos pos) {
        containerStates.remove(pos);
        System.out.println("Cleared state for container at " + pos);
    }

    /**
     * Очищает все состояния (при выходе из мира)
     */
    public void clearAllStates() {
        containerStates.clear();
        System.out.println("Cleared all container states");
    }

    /**
     * Получает количество отслеживаемых контейнеров
     */
    public int getTrackedContainerCount() {
        return containerStates.size();
    }

    // 🔴 Вспомогательный класс для хранения состояния контейнера
    private record ContainerState(List<ItemStack> slots, long timestamp) {}
}
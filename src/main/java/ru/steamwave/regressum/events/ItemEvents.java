package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import ru.steamwave.regressum.db.DbManager;
import ru.steamwave.regressum.model.ItemLogAction;
import ru.steamwave.regressum.storage.ActionQueue;
import ru.steamwave.regressum.storage.CacheManager;
import ru.steamwave.regressum.utils.InspectorUtil;

public class ItemEvents {

    private final DbManager dbManager;

    public ItemEvents(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Игрок подобрал предмет с земли
     * В NeoForge 1.21.1 используется ItemEntityPickupEvent
     */
    @SubscribeEvent
    public void onItemPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event) {
        if (event.getPlayer().level().isClientSide()) return;

        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack originalStack = event.getOriginalStack(); // Исходный стак до подбора

        if (InspectorUtil.isInspecting(player.getUUID())) return;

        logItemAction(
                player.level(),
                itemEntity.blockPosition(),
                player,
                originalStack,
                "pickup"
        );
    }

    /**
     * Игрок выбросил предмет (Q)
     * В NeoForge 1.21.1 используется PlayerDropItemEvent
     */
    @SubscribeEvent
    public void onItemToss(net.neoforged.neoforge.event.entity.item.ItemTossEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();

        if (InspectorUtil.isInspecting(player.getUUID())) return;

        logItemAction(
                player.level(),
                player.blockPosition(),
                player,
                stack,
                "drop"
        );
    }

    /**
     * Предмет испортился (пропал через 5 минут)
     */
    @SubscribeEvent
    public void onItemExpire(ItemExpireEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        ItemEntity itemEntity = event.getEntity();
        ItemStack stack = itemEntity.getItem();

        logItemAction(
                itemEntity.level(),
                itemEntity.blockPosition(),
                null, // Нет игрока - системное событие
                stack,
                "expire"
        );
    }

    /**
     * Игрок сломал предмет (использовал до конца)
     */
    @SubscribeEvent
    public void onItemBreak(PlayerDestroyItemEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack stack = event.getOriginal();

        if (InspectorUtil.isInspecting(player.getUUID())) return;

        logItemAction(
                player.level(),
                player.blockPosition(),
                player,
                stack,
                "break"
        );
    }

    /**
     * Игрок выбросил предмет смертью
     */
    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.Clone event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Логируем все предметы которые выпали при смерти
        if (original instanceof ServerPlayer serverPlayer) {
            ServerLevel level = serverPlayer.serverLevel();

            // Получаем все выброшенные предметы вокруг места смерти
            level.getEntitiesOfClass(
                    ItemEntity.class,
                    serverPlayer.getBoundingBox().inflate(10)
            ).forEach(itemEntity -> {
                ItemStack stack = itemEntity.getItem();
                logItemAction(
                        level,
                        itemEntity.blockPosition(),
                        serverPlayer,
                        stack,
                        "death_drop"
                );
            });
        }
    }

    /**
     * Основной метод логирования (ТВОЙ КОД)
     */
    private void logItemAction(Level level, BlockPos pos, Player player,
                               ItemStack stack, String actionType) {
        if (stack.isEmpty() || stack.getCount() <= 0) return;

        long time = System.currentTimeMillis();
        String world = level.dimension().location().toString();

        String playerName = player != null ? player.getName().getString() : "system";
        String playerUUID = player != null ? player.getUUID().toString() : "system";

        System.out.println("LOGGING: " + playerName + " " + actionType + " " +
                stack.getCount() + "x " + stack.getItem().toString() + " at " + pos);

        CacheManager.getInstance().putItemAction(
                null,
                player != null ? player.getUUID() : null,
                playerName,
                stack.getItem().toString(),
                world,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                time,
                stack.getCount(),
                actionType
        );

        ActionQueue.queue.add(new ItemLogAction(
                player != null ? player.getUUID() : null,
                playerName,
                stack.getItem().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                world,
                time,
                actionType,
                stack.getCount()
        ));
    }
}
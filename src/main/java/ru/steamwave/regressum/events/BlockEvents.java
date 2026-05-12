package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.steamwave.regressum.db.DbManager;
import ru.steamwave.regressum.model.BlockLogAction;
import ru.steamwave.regressum.storage.ActionQueue;
import ru.steamwave.regressum.storage.CacheManager;

import java.util.UUID;

public class BlockEvents {
    private static final Logger LOGGER = LogManager.getLogger("Regressum");
    private final DbManager dbManager;

    public BlockEvents(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            String worldName = level.dimension().location().toString(); // Преобразуем ResourceLocation в String
            logBlockAction(
                    event.getPlayer().getUUID(),
                    event.getPlayer().getGameProfile().getName(),
                    event.getState().getBlock().getName().getString(),
                    worldName,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    "BREAK"
            );
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() != null && event.getLevel() instanceof ServerLevel level) {
            String worldName = level.dimension().location().toString(); // Преобразуем ResourceLocation в String
            logBlockAction(
                    event.getEntity().getUUID(),
                    event.getEntity().getScoreboardName(),
                    event.getState().getBlock().getName().getString(),
                    worldName,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    "PLACE"
            );
        }
    }

    @SubscribeEvent
    public void onBlockInteract(BlockEvent.BlockToolModificationEvent event) {
        if (event.getPlayer() != null && event.getLevel() instanceof ServerLevel level) {
            String worldName = level.dimension().location().toString(); // Преобразуем ResourceLocation в String
            logBlockAction(
                    event.getPlayer().getUUID(),
                    event.getPlayer().getGameProfile().getName(),
                    event.getState().getBlock().getName().getString(),
                    worldName,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    "INTERACT"
            );
        }
    }
    @SubscribeEvent
    public void onTNTExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        String worldName = level.dimension().location().toString();

        for (BlockPos pos : event.getAffectedBlocks()) {
            String blockName = level.getBlockState(pos).getBlock().getName().getString();

            if (blockName.equals("Air")) continue;
            // Логируем через общий метод
            logBlockAction(
                    null,           // UUID игрока нет
                    "SYSTEM",       // изменение не от игрока
                    blockName,
                    worldName,
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    "EXPLOSION"     // actionType
            );
        }
    }
    @SubscribeEvent
    public void onEntityAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        if (!(event.getTarget().level() instanceof ServerLevel level)) return;

        String worldName = level.dimension().location().toString();

        if (event.getTarget() instanceof ItemFrame || event.getTarget() instanceof ArmorStand) {
            logBlockAction(
                    player.getUUID(),
                    player.getGameProfile().getName(),
                    event.getTarget().getName().getString(), // Название сущности как "блока"
                    worldName,
                    event.getTarget().blockPosition().getX(),
                    event.getTarget().blockPosition().getY(),
                    event.getTarget().blockPosition().getZ(),
                    "BREAK"
            );
        }}
    private void logBlockAction(UUID playerUuid, String playerName, String block,
                                String world, int x, int y, int z, String actionType) {
        long time = System.currentTimeMillis();

        // Кэшируем
        CacheManager.getInstance().putBlockAction(
                null, // dbId пока нет
                playerUuid,
                playerName,
                block,
                world,
                x, y, z,
                time,
                actionType
        );

        // Добавляем в очередь для асинхронной записи в БД
        ActionQueue.queue.add(new BlockLogAction(
                playerUuid,
                playerName,
                block,
                x, y, z,
                world,
                time,
                actionType
        ));

        LOGGER.info("Block action [{}] called for: {} at {},{},{} in {}",
                actionType, playerName, x, y, z, world);
        LOGGER.info("Queue size: {}", ActionQueue.queue.size());
    }
}
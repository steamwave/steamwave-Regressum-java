package ru.steamwave.regressum.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.steamwave.regressum.db.DbManager;
import ru.steamwave.regressum.model.EntityLogAction;
import ru.steamwave.regressum.storage.ActionQueue;
import ru.steamwave.regressum.storage.CacheManager;

import java.util.UUID;

public class EntityEvents {
    private final DbManager dbManager;

    public EntityEvents(DbManager dbManager) {
        this.dbManager = dbManager;
    }
    // private static final Logger LOGGER = LogManager.getLogger("Regressum");

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        try {
            Entity entity = event.getEntity();
            String entityName;
            if (entity instanceof Player player) {
                entityName = player.getGameProfile().getName(); // имя игрока
            } else {
                // Берём безопасный серверный ID сущности (например, "minecraft:zombie")
                ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                entityName = rl != null ? rl.toString() : "unknown";
            }

            // LOGGER.debug("EntityJoinLevelEvent triggered for entity: {}", entityName);

            if (event.getLevel() instanceof ServerLevel level && entity instanceof LivingEntity) {
                String worldName = level.dimension().location().toString();
                String victimName = entityName;
                String victimType = entityName;

                logEntityAction(
                        null,
                        "SYSTEM",
                        "SYSTEM",
                        victimType,
                        victimName,
                        worldName,
                        entity.blockPosition().getX(),
                        entity.blockPosition().getY(),
                        entity.blockPosition().getZ(),
                        "SPAWN"
                );
            }
        } catch (Exception e) {
        //    LOGGER.error("Error in onEntitySpawn for entity: {}: {}", event.getEntity(), e.getMessage());
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        try {
            LivingEntity victim = event.getEntity();
            String victimName;
            if (victim instanceof Player player) {
                victimName = player.getGameProfile().getName(); // имя игрока
            } else {
                ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
                victimName = rl != null ? rl.toString() : "unknown";
            }

          //  LOGGER.debug("LivingDeathEvent triggered for entity: {}", victimName);

            if (event.getEntity().level() instanceof ServerLevel level) {
                // Источник урона
                Entity source = event.getSource().getEntity();

                String worldName = level.dimension().location().toString();

                // Определяем убийцу
                String killerName;
                UUID killerUUID = null;
                String killerType;

                if (source instanceof Player player) {
                    killerName = player.getGameProfile().getName();
                    killerUUID = player.getUUID();
                    killerType = "PLAYER";
                } else if (source instanceof LivingEntity living) {
                    ResourceLocation rl = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
                    killerName = rl != null ? rl.toString() : "unknown";
                    killerType = "MOB";
                } else {
                    killerName = event.getSource().getMsgId() != null && !event.getSource().getMsgId().isEmpty()
                            ? event.getSource().getMsgId()
                            : "ENVIRONMENT";
                    killerType = "ENVIRONMENT";
                }

                // Определяем жертву
                String victimType = victimName;

                logEntityAction(
                        killerUUID,
                        killerName,
                        killerType,
                        victimType,
                        victimName,
                        worldName,
                        victim.blockPosition().getX(),
                        victim.blockPosition().getY(),
                        victim.blockPosition().getZ(),
                        "KILL"
                );
            }
        } catch (Exception e) {
           // LOGGER.error("Error in onEntityDeath for entity: {}: {}", event.getEntity(), e.getMessage());
        }
    }

    private void logEntityAction(UUID killerUUID, String killerName, String killerType,
                                 String victimType, String victimName, String world,
                                 int x, int y, int z, String actionType) {
        long time = System.currentTimeMillis();

        //LOGGER.info("logEntityAction called with killer: {}, victim: {}, action: {}", killerName, victimName, actionType);

        // Кэшируем
        try {
            CacheManager.getInstance().putEntityAction(
                    null,
                    killerUUID,
                    killerName,
                    killerType,
                    victimType,
                    victimName,
                    world,
                    x, y, z,
                    time
            );
        } catch (Exception e) {
            //  LOGGER.error("Error in CacheManager.putEntityAction: {}", e.getMessage());
        }

        // Добавляем в очередь
        EntityLogAction action = new EntityLogAction(
                killerUUID,
                killerName,
                killerType,
                victimType,
                victimName,
                x, y, z,
                world,
                time,
                actionType
        );

        ActionQueue.queue.add(action);
        //LOGGER.info("Added to ActionQueue: {}", action);

        //LOGGER.info("Entity action [{}] called: {} (type: {}) affected {} (type: {}) at {},{},{} in {}",
        //        actionType, killerName, killerType, victimName, victimType, x, y, z, world);
        //  LOGGER.info("Queue size: {}", ActionQueue.queue.size());
    }
}
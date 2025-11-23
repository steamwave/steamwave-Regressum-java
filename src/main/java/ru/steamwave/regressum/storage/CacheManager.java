package ru.steamwave.regressum.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CacheManager {

    private static final Logger LOGGER = LogManager.getLogger(CacheManager.class);

    private static CacheManager instance;

    private final int maxHistoryPerPos;
    private final int maxHistoryPerPlayer;
    private final long ttlMillis;

    private final ConcurrentMap<PosKey, ConcurrentLinkedDeque<BlockLogEntry>> posMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<BlockLogEntry>> playerMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PosKey, ConcurrentLinkedDeque<EntityLogEntry>> entityPosMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<EntityLogEntry>> killerMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PosKey, ConcurrentLinkedDeque<ItemLogEntry>> itemPosMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<ItemLogEntry>> itemPlayerMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<PosKey, ConcurrentLinkedDeque<ContainerLogEntry>> containerPosMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ConcurrentLinkedDeque<ContainerLogEntry>> containerPlayerMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "CacheManager-Cleaner");
        t.setDaemon(true);
        return t;
    });

    private CacheManager(int maxHistoryPerPos, int maxHistoryPerPlayer, long ttlMillis, long cleanupIntervalMillis) {
        this.maxHistoryPerPos = maxHistoryPerPos;
        this.maxHistoryPerPlayer = maxHistoryPerPlayer;
        this.ttlMillis = ttlMillis;

        cleaner.scheduleAtFixedRate(this::cleanupTask, cleanupIntervalMillis, cleanupIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public static synchronized CacheManager init(int maxPerPos, int maxPerPlayer, long ttlMillis, long cleanupIntervalMillis) {
        if (instance == null) {
            instance = new CacheManager(maxPerPos, maxPerPlayer, ttlMillis, cleanupIntervalMillis);
        }
        return instance;
    }

    public static CacheManager getInstance() {
        if (instance == null) throw new IllegalStateException("CacheManager not initialized");
        return instance;
    }

    // Блоки
    public void putBlockAction(Long dbId, UUID playerUuid, String playerName,
                               String blockType, String world, int x, int y, int z,
                               long timestamp, String actionType) {
        PosKey pos = new PosKey(world, x, y, z);
        BlockLogEntry entry = new BlockLogEntry(dbId, playerUuid, playerName, blockType, x, y, z, timestamp, actionType);

        posMap.compute(pos, (k, deque) -> {
            if (deque == null) deque = new ConcurrentLinkedDeque<>();
            deque.addFirst(entry);
            while (deque.size() > maxHistoryPerPos) deque.pollLast();
            return deque;
        });

        if (playerUuid != null) {
            playerMap.compute(playerUuid, (k, deque) -> {
                if (deque == null) deque = new ConcurrentLinkedDeque<>();
                deque.addFirst(entry);
                while (deque.size() > maxHistoryPerPlayer) deque.pollLast();
                return deque;
            });
        }
    }

    // Сущности
    public void putEntityAction(Long dbId, UUID killerUUID, String killerName, String killerType,
                                String victimType, String victimName, String world,
                                int x, int y, int z, long timestamp) {
        PosKey pos = new PosKey(world, x, y, z);
        EntityLogEntry entry = new EntityLogEntry(dbId, killerUUID, killerName, killerType, victimType, victimName, world, x, y, z, timestamp);

        entityPosMap.compute(pos, (k, deque) -> {
            if (deque == null) deque = new ConcurrentLinkedDeque<>();
            deque.addFirst(entry);
            while (deque.size() > maxHistoryPerPos) deque.pollLast();
            return deque;
        });

        if (killerUUID != null) {
            killerMap.compute(killerUUID, (k, deque) -> {
                if (deque == null) deque = new ConcurrentLinkedDeque<>();
                deque.addFirst(entry);
                while (deque.size() > maxHistoryPerPlayer) deque.pollLast();
                return deque;
            });
        }
    }

    // Предметы
    public void putItemAction(Long dbId, UUID playerUuid, String playerName,
                              String itemName, String world, int x, int y, int z,
                              long timestamp, int amount, String actionType) {
        PosKey pos = new PosKey(world, x, y, z);
        ItemLogEntry entry = new ItemLogEntry(dbId, playerUuid, playerName, itemName, x, y, z, timestamp, amount, actionType);

        itemPosMap.compute(pos, (k, deque) -> {
            if (deque == null) deque = new ConcurrentLinkedDeque<>();
            deque.addFirst(entry);
            while (deque.size() > maxHistoryPerPos) deque.pollLast();
            return deque;
        });

        if (playerUuid != null) {
            itemPlayerMap.compute(playerUuid, (k, deque) -> {
                if (deque == null) deque = new ConcurrentLinkedDeque<>();
                deque.addFirst(entry);
                while (deque.size() > maxHistoryPerPlayer) deque.pollLast();
                return deque;
            });
        }
    }

    // Контейнеры
    public void putContainerAction(Long dbId, UUID playerUuid, String playerName,
                                   String itemName, String world, int x, int y, int z,
                                   long timestamp, String actionType, int amount,
                                   String containerType, String containerData) {
        PosKey pos = new PosKey(world, x, y, z);
        ContainerLogEntry entry = new ContainerLogEntry(dbId, playerUuid, playerName, itemName,
                x, y, z, timestamp, actionType, amount,
                containerType, containerData);

        containerPosMap.compute(pos, (k, deque) -> {
            if (deque == null) deque = new ConcurrentLinkedDeque<>();
            deque.addFirst(entry);
            while (deque.size() > maxHistoryPerPos) deque.pollLast();
            return deque;
        });

        if (playerUuid != null) {
            containerPlayerMap.compute(playerUuid, (k, deque) -> {
                if (deque == null) deque = new ConcurrentLinkedDeque<>();
                deque.addFirst(entry);
                while (deque.size() > maxHistoryPerPlayer) deque.pollLast();
                return deque;
            });
        }
    }

    // Методы получения данных (блоки)
    public BlockLogEntry getLatestAtPos(String world, int x, int y, int z) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<BlockLogEntry> deque = posMap.get(pos);
        return (deque == null) ? null : deque.peekFirst();
    }

    public List<BlockLogEntry> getHistoryAtPos(String world, int x, int y, int z, int limit) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<BlockLogEntry> deque = posMap.get(pos);
        if (deque == null) return List.of();
        List<BlockLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (BlockLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    public List<BlockLogEntry> getRecentByPlayer(UUID playerUuid, int limit) {
        ConcurrentLinkedDeque<BlockLogEntry> deque = playerMap.get(playerUuid);
        if (deque == null) return List.of();
        List<BlockLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (BlockLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    // Методы получения данных (сущности)
    public EntityLogEntry getLatestEntityAtPos(String world, int x, int y, int z) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<EntityLogEntry> deque = entityPosMap.get(pos);
        return (deque == null) ? null : deque.peekFirst();
    }

    public List<EntityLogEntry> getEntityHistoryAtPos(String world, int x, int y, int z, int limit) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<EntityLogEntry> deque = entityPosMap.get(pos);
        if (deque == null) return List.of();
        List<EntityLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (EntityLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    public List<EntityLogEntry> getRecentByKiller(UUID killerUUID, int limit) {
        ConcurrentLinkedDeque<EntityLogEntry> deque = killerMap.get(killerUUID);
        if (deque == null) return List.of();
        List<EntityLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (EntityLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    // Методы получения данных (предметы)
    public ItemLogEntry getLatestItemAtPos(String world, int x, int y, int z) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<ItemLogEntry> deque = itemPosMap.get(pos);
        return (deque == null) ? null : deque.peekFirst();
    }

    public List<ItemLogEntry> getItemHistoryAtPos(String world, int x, int y, int z, int limit) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<ItemLogEntry> deque = itemPosMap.get(pos);
        if (deque == null) return List.of();
        List<ItemLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (ItemLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    public List<ItemLogEntry> getRecentItemByPlayer(UUID playerUuid, int limit) {
        ConcurrentLinkedDeque<ItemLogEntry> deque = itemPlayerMap.get(playerUuid);
        if (deque == null) return List.of();
        List<ItemLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (ItemLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    // Методы получения данных (контейнеры)
    public ContainerLogEntry getLatestContainerAtPos(String world, int x, int y, int z) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<ContainerLogEntry> deque = containerPosMap.get(pos);
        return (deque == null) ? null : deque.peekFirst();
    }

    public List<ContainerLogEntry> getContainerHistoryAtPos(String world, int x, int y, int z, int limit) {
        PosKey pos = new PosKey(world, x, y, z);
        ConcurrentLinkedDeque<ContainerLogEntry> deque = containerPosMap.get(pos);
        if (deque == null) return List.of();
        List<ContainerLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (ContainerLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    public List<ContainerLogEntry> getRecentContainerByPlayer(UUID playerUuid, int limit) {
        ConcurrentLinkedDeque<ContainerLogEntry> deque = containerPlayerMap.get(playerUuid);
        if (deque == null) return List.of();
        List<ContainerLogEntry> out = new ArrayList<>(Math.min(limit, deque.size()));
        int i = 0;
        for (ContainerLogEntry e : deque) {
            out.add(e);
            if (++i >= limit) break;
        }
        return out;
    }

    // Отметка отката
    public void markRolledBackByDbId(long dbId) {
        // Блоки
        posMap.forEach((pos, deque) -> {
            for (BlockLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });
        playerMap.forEach((uuid, deque) -> {
            for (BlockLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });

        // Сущности
        entityPosMap.forEach((pos, deque) -> {
            for (EntityLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });
        killerMap.forEach((uuid, deque) -> {
            for (EntityLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });

        // Предметы
        itemPosMap.forEach((pos, deque) -> {
            for (ItemLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });
        itemPlayerMap.forEach((uuid, deque) -> {
            for (ItemLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });

        // Контейнеры
        containerPosMap.forEach((pos, deque) -> {
            for (ContainerLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });
        containerPlayerMap.forEach((uuid, deque) -> {
            for (ContainerLogEntry e : deque) {
                if (e.dbId != null && e.dbId == dbId) e.rolledBack = true;
            }
        });
    }

    // Очистка
    private void cleanupTask() {
        long cutoff = Instant.now().toEpochMilli() - ttlMillis;

        // Блоки
        cleanupDequeMap(posMap, cutoff);
        cleanupDequeMap(playerMap, cutoff);

        // Сущности
        cleanupDequeMap(entityPosMap, cutoff);
        cleanupDequeMap(killerMap, cutoff);

        // Предметы
        cleanupDequeMap(itemPosMap, cutoff);
        cleanupDequeMap(itemPlayerMap, cutoff);

        // Контейнеры
        cleanupDequeMap(containerPosMap, cutoff);
        cleanupDequeMap(containerPlayerMap, cutoff);

        LOGGER.info("cleanupTask: blocks={}, entities={}, items={}, containers={}",
                posMap.size(), entityPosMap.size(), itemPosMap.size(), containerPosMap.size());
    }

    private <T> void cleanupDequeMap(ConcurrentMap<?, ConcurrentLinkedDeque<T>> map, long cutoff) {
        map.forEach((key, deque) -> {
            if (deque == null) return;
            while (true) {
                T last = deque.peekLast();
                if (last == null) break;
                if (getTimestamp(last) < cutoff) {
                    deque.pollLast();
                } else break;
            }
            if (deque.isEmpty()) map.remove(key, deque);
        });
    }

    private long getTimestamp(Object entry) {
        if (entry instanceof BlockLogEntry) return ((BlockLogEntry) entry).timestamp;
        if (entry instanceof EntityLogEntry) return ((EntityLogEntry) entry).timestamp;
        if (entry instanceof ItemLogEntry) return ((ItemLogEntry) entry).timestamp;
        if (entry instanceof ContainerLogEntry) return ((ContainerLogEntry) entry).timestamp;
        return 0;
    }

    public void removeAllAtPos(String world, int x, int y, int z) {
        PosKey pos = new PosKey(world, x, y, z);
        posMap.remove(pos);
        entityPosMap.remove(pos);
        itemPosMap.remove(pos);
        containerPosMap.remove(pos);
    }

    public void shutdown() {
        cleaner.shutdownNow();
    }

    public static class PosKey {
        private final String world;
        private final int x, y, z;

        public PosKey(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PosKey posKey = (PosKey) o;
            return x == posKey.x && y == posKey.y && z == posKey.z && world.equals(posKey.world);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + y;
            result = 31 * result + z;
            return result;
        }
    }
}
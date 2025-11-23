package ru.steamwave.regressum.storage;

import java.util.UUID;

public class EntityLogEntry {
    public final Long dbId;           // можно null пока не записано в БД
    public final UUID killerUUID;     // UUID убийцы, может быть null (например, для спавна или смерти от окружения)
    public final String killerName;   // Имя убийцы (или "SYSTEM"/"ENVIRONMENT")
    public final String killerType;   // Тип убийцы (например, "minecraft:zombie" или "ENVIRONMENT")
    public final String victimType;   // Тип жертвы (например, "minecraft:player" или "minecraft:zombie")
    public final String victimName;   // Имя жертвы (например, имя игрока или название моба)
    public final String world;        // Название мира
    public final int x, y, z;         // Координаты события
    public final long timestamp;      // Время события (epoch millis)
    public volatile boolean rolledBack; // Пометка, если действие откатили

    public EntityLogEntry(Long dbId, UUID killerUUID, String killerName, String killerType,
                          String victimType, String victimName, String world,
                          int x, int y, int z, long timestamp) {
        this.dbId = dbId;
        this.killerUUID = killerUUID;
        this.killerName = killerName;
        this.killerType = killerType;
        this.victimType = victimType;
        this.victimName = victimName;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.rolledBack = false;
    }

    public String posKey() {
        return x + "," + y + "," + z;
    }

}

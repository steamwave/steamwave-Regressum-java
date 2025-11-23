package ru.steamwave.regressum.model;

import java.util.UUID;

public class EntityLogAction extends LogAction {
    public final UUID killerUUID;     // UUID убийцы, может быть null
    public final String killerName;   // Имя убийцы (или "SYSTEM"/"ENVIRONMENT")
    public final String killerType;   // Тип убийцы (например, "minecraft:zombie" или "ENVIRONMENT")
    public final String victimType;   // Тип жертвы (например, "minecraft:player" или "minecraft:zombie")
    public final String victimName;   // Имя жертвы (например, имя игрока или название моба)
    public final String world;        // Название мира
    public final int x, y, z;         // Координаты события
    public final String actionType;   // Тип действия ("SPAWN" или "KILL")

    public EntityLogAction(UUID killerUUID, String killerName, String killerType,
                           String victimType, String victimName, int x, int y, int z,
                           String world, long time, String actionType) {
        super(time);
        this.killerUUID = killerUUID;
        this.killerName = killerName;
        this.killerType = killerType;
        this.victimType = victimType;
        this.victimName = victimName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.actionType = actionType;
    }

    @Override
    public String getType() {
        return "entity";
    }
}
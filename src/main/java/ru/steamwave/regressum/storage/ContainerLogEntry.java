package ru.steamwave.regressum.storage;

import java.util.UUID;

public class ContainerLogEntry {
    public final Long dbId;          // можно null пока не записано в БД
    public final UUID playerUuid;    // лучше хранить UUID, но может быть null
    public final String playerName;  // имя для удобства
    public final String itemName;
    public final int x, y, z;
    public final long timestamp;     // epoch millis
    public final String actionType;  // "place", "take"
    public final int amount;
    public final String containerType; // тип контейнера
    public final String containerData; // дополнительные данные
    public volatile boolean rolledBack; // пометка, если откатили

    public ContainerLogEntry(Long dbId, UUID playerUuid, String playerName,
                             String itemName, int x, int y, int z, long timestamp,
                             String actionType, int amount, String containerType, String containerData) {
        this.dbId = dbId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.itemName = itemName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.actionType = actionType;
        this.amount = amount;
        this.containerType = containerType;
        this.containerData = containerData;
        this.rolledBack = false;
    }

    public String posKey() {
        return x + "," + y + "," + z;
    }
}
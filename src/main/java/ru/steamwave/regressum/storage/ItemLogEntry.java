package ru.steamwave.regressum.storage;

import java.util.UUID;

public class ItemLogEntry {
    public final Long dbId;          // может быть null пока не записано в БД
    public final UUID playerUuid;    // может быть null
    public final String playerName;  // имя для удобства
    public final String itemName;
    public final int x, y, z;
    public final long timestamp;     // epoch millis
    public final int amount;
    public volatile boolean rolledBack;
    public final String actionType;// пометка, если откатили

    public ItemLogEntry(Long dbId, UUID playerUuid, String playerName,
                        String itemName, int x, int y, int z, long timestamp, int amount, String actionType) {
        this.dbId = dbId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.itemName = itemName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.amount = amount;
        this.rolledBack = false;
        this.actionType = actionType;
    }

    public String posKey() {
        return x + "," + y + "," + z;
    }
}
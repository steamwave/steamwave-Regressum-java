// BlockLogEntry.java
package ru.steamwave.regressum.storage;

import java.util.UUID;

public class BlockLogEntry {
    public final Long dbId;          // можно null пока не записано в БД
    public final UUID playerUuid;    // лучше хранить UUID, но может быть null
    public final String playerName;  // имя для удобства
    public final String blockType;
    public final int x, y, z;
    public final long timestamp;     // epoch millis
    public volatile boolean rolledBack;
    public final String actionType;// пометка, если откатили

    public BlockLogEntry(Long dbId, UUID playerUuid, String playerName,
                         String blockType, int x, int y, int z, long timestamp, String actionType) {
        this.dbId = dbId;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
        this.rolledBack = false;
        this.actionType = actionType;
    }

    public String posKey() {
        return x + "," + y + "," + z;
    }
}

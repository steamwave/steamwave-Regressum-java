package ru.steamwave.regressum.model;

import java.util.UUID;

public class BlockLogAction extends LogAction {
    public final UUID playerUuid;
    public final String playerName;
    public final String blockType;
    public final int x, y, z;
    public final String world;
    public final String actionType; // <- добавляем поле

    public BlockLogAction(UUID playerUuid, String playerName, String blockType,
                          int x, int y, int z, String world, long time, String actionType) {
        super(time);
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.actionType = actionType; // <- сохраняем
    }

    @Override
    public String getType() {
        return "block";
    }
}

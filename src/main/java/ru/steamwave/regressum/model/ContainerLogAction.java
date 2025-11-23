package ru.steamwave.regressum.model;

import java.util.UUID;

public class ContainerLogAction extends LogAction {
    public final UUID playerUuid;
    public final String playerName;
    public final String itemName;
    public final int x, y, z;
    public final String world;
    public final String actionType; // "place", "take"
    public final int amount;
    public final String containerType; // "chest", "ender_chest", etc.
    public final String containerData;

    public ContainerLogAction(UUID playerUuid, String playerName, String itemName,
                              int x, int y, int z, String world, long time,
                              String actionType, int amount, String containerType, String containerData) {
        super(time);
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.itemName = itemName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.actionType = actionType;
        this.amount = amount;
        this.containerType = containerType;
        this.containerData = containerData;
    }

    @Override
    public String getType() {
        return "container";
    }
}
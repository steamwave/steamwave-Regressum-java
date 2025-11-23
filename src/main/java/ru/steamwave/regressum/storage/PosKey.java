// PosKey.java
package ru.steamwave.regressum.storage;

import java.util.Objects;

public final class PosKey {
    public final String world;
    public final int x, y, z;

    public PosKey(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PosKey)) return false;
        PosKey p = (PosKey) o;
        return x == p.x && y == p.y && z == p.z && Objects.equals(world, p.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }

    @Override
    public String toString() {
        return world + ":" + x + "," + y + "," + z;
    }
}

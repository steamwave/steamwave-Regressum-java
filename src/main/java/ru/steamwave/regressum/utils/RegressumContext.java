package ru.steamwave.regressum.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;

public class RegressumContext {
    private static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos> CURRENT_POS = new ThreadLocal<>();

    public static void setCurrentPlayer(Player player) {
        CURRENT_PLAYER.set(player);
    }

    public static void setPos(BlockPos pos) {
        CURRENT_POS.set(pos);
    }

    @Nullable
    public static Player getCurrentPlayer() {
        return CURRENT_PLAYER.get();
    }

    @Nullable
    public static BlockPos getPos() {
        return CURRENT_POS.get();
    }

    /**
     * Важно вызывать clear() ВСЕГДА после завершения действия,
     * иначе данные игрока "подвиснут" в потоке сервера.
     */
    public static void clear() {
        CURRENT_PLAYER.remove();
        CURRENT_POS.remove();
    }
}
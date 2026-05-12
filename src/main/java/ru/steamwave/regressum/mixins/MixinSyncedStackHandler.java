package ru.steamwave.regressum.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.steamwave.regressum.utils.RegressumContext;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;

// Миксимся во внутренний класс SyncedStackHandler
@Mixin(targets = "com.simibubi.create.foundation.item.SmartInventory$SyncedStackHandler", remap = false)
public abstract class MixinSyncedStackHandler {

    @Shadow
    private SyncedBlockEntity blockEntity;

    @Inject(method = "onContentsChanged", at = @At("TAIL"))
    private void onChanged(int slot, CallbackInfo ci) {
        // Достаем координаты блока прямо из сущности
        BlockPos pos = blockEntity.getBlockPos();

        // Пытаемся понять, какой игрок это сделал (из нашего контекста)
        Player player = RegressumContext.getCurrentPlayer();

        if (player != null) {
            System.out.println(String.format(
                    "[Regressum] SMART-LOG: Игрок %s изменил инвентарь Create на %s (слот %d)",
                    player.getName().getString(),
                    pos.toShortString(),
                    slot
            ));
        } else {
            // Если игрок null, значит это сделал механизм (воронка, рука, конвейер)
            System.out.println("[Regressum] SMART-LOG: Механизм изменил блок на " + pos.toShortString());
        }
    }
}
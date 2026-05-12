package ru.steamwave.regressum.mixins_ols;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.steamwave.regressum.utils.RegressumContext;

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu {
    @Inject(method = "clicked", at = @At("HEAD"))
    private void beforeClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        RegressumContext.setCurrentPlayer(player);
    }

    @Inject(method = "clicked", at = @At("TAIL"))
    private void afterClick(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        RegressumContext.clear();
    }
}
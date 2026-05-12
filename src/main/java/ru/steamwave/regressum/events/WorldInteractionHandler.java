package ru.steamwave.regressum.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import ru.steamwave.regressum.utils.RegressumContext;

@EventBusSubscriber(modid = "regressum")
public class WorldInteractionHandler {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Запоминаем игрока ПЕРЕД тем, как блок выполнит свою логику (например, заберет предмет)
        RegressumContext.setCurrentPlayer(event.getEntity());

        // Мы не очищаем контекст здесь сразу,
        // так как логика BlockEntity сработает чуть позже в этом же тике.
    }

    @SubscribeEvent
    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        RegressumContext.setCurrentPlayer(event.getEntity());
    }
}
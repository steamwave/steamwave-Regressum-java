package ru.steamwave.regressum.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import ru.steamwave.regressum.storage.BlockLogEntry;
import ru.steamwave.regressum.storage.ItemLogEntry;
import ru.steamwave.regressum.utils.InspectorUtil;
import ru.steamwave.regressum.utils.InspectorUtil.InspectorState;
import ru.steamwave.regressum.db.InspectorService;
import ru.steamwave.regressum.db.DbManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class InspectorEvents {

    private static final int PAGE_SIZE = 8;
    private static final InspectorService inspectorService = new InspectorService(new DbManager());

    // Цветовая схема
    private static final ChatFormatting COLOR_PRIMARY = ChatFormatting.GOLD;      // Оранжевый - основной
    private static final ChatFormatting COLOR_SECONDARY = ChatFormatting.GRAY;    // Серый - второстепенный
    private static final ChatFormatting COLOR_ACCENT = ChatFormatting.AQUA;       // Голубой - акценты
    private static final ChatFormatting COLOR_TEXT = ChatFormatting.WHITE;        // Белый - основной текст
    private static final ChatFormatting COLOR_TIME = ChatFormatting.DARK_GRAY;    // Темно-серый - время

    // 🔴 ИСПРАВЛЕНИЕ: добавляем static к методам событий
    @SubscribeEvent
    public  void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        InspectorState state = InspectorUtil.getState(player.getUUID());
        if (!state.enabled) return;

        BlockPos pos = event.getPos();
        BlockState blockState = event.getLevel().getBlockState(pos);

        state.lastPos = pos;
        state.logType = "block";

        showBlockHistory(player, pos, blockState, state.currentPage);

        event.setCanceled(true);
        player.swing(event.getHand(), false);
    }

    // 🔴 ИСПРАВЛЕНИЕ: добавляем static к методам событий
    @SubscribeEvent
    public  void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        InspectorState state = InspectorUtil.getState(player.getUUID());
        if (!state.enabled) return;

        BlockPos pos = event.getPos();
        BlockEntity be = event.getLevel().getBlockEntity(pos);

        state.lastPos = pos;
        state.logType = (be != null) ? "container" : "block";

        if (be != null) {
            showContainerHistory(player, pos, state.currentPage);
        } else {
            showBlockHistory(player, pos, event.getLevel().getBlockState(pos), state.currentPage);
        }

        event.setCanceled(true);
        player.swing(event.getHand(), false);
    }

    public static void showBlockHistory(ServerPlayer player, BlockPos pos, BlockState block, int page) {
        List<BlockLogEntry> history = inspectorService.getAllBlockHistory(
                player.level().dimension().location().toString(),
                pos.getX(), pos.getY(), pos.getZ()
        );

        if (history.isEmpty()) {
            sendMessage(player,
                    Component.literal("❌ ").withStyle(COLOR_PRIMARY)
                            .append(Component.literal("Нет истории для блока ").withStyle(COLOR_SECONDARY))
                            .append(Component.literal(block.getBlock().getName().getString()).withStyle(COLOR_TEXT))
            );
            return;
        }

        paginateBlockHistory(player, history, pos, block.getBlock().getName().getString(), page);
    }

    public static void showContainerHistory(ServerPlayer player, BlockPos pos, int page) {
        List<ItemLogEntry> history = inspectorService.getAllItemHistory(
                player.level().dimension().location().toString(),
                pos.getX(), pos.getY(), pos.getZ()
        );

        if (history.isEmpty()) {
            sendMessage(player,
                    Component.literal("❌ ").withStyle(COLOR_PRIMARY)
                            .append(Component.literal("Нет истории контейнера на позиции ").withStyle(COLOR_SECONDARY))
                            .append(Component.literal(formatPos(pos)).withStyle(COLOR_TEXT))
            );
            return;
        }

        paginateItemHistory(player, history, pos, page);
    }

    private static void paginateBlockHistory(ServerPlayer player, List<BlockLogEntry> history, BlockPos pos, String blockName, int page) {
        int totalPages = (int) Math.ceil((double) history.size() / PAGE_SIZE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        // Заголовок
        MutableComponent header = Component.literal("📦 ").withStyle(COLOR_PRIMARY)
                .append(Component.literal("История блока: ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(blockName).withStyle(COLOR_TEXT))
                .append(Component.literal(" | ").withStyle(COLOR_SECONDARY))
                .append(Component.literal("Страница " + (page + 1) + "/" + totalPages).withStyle(COLOR_ACCENT));

        sendMessage(player, header);

        // 🔴 ИСПРАВЛЕНИЕ: history отсортирован от старых к новым (ASC)
        // Но мы хотим показывать сначала новые, поэтому идем с конца
        int totalEntries = history.size();
        int start = Math.max(0, totalEntries - (page + 1) * PAGE_SIZE);
        int end = Math.min(totalEntries, totalEntries - page * PAGE_SIZE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        long now = System.currentTimeMillis();

        // 🔴 Выводим записи в обратном порядке (от новых к старым)
        for (int i = end - 1; i >= start; i--) {
            BlockLogEntry entry = history.get(i);
            sendBlockEntry(player, entry, sdf, now);
        }

        sendNavigation(player, page, totalPages, "block");
    }

    private static void paginateItemHistory(ServerPlayer player, List<ItemLogEntry> history, BlockPos pos, int page) {
        int totalPages = (int) Math.ceil((double) history.size() / PAGE_SIZE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        // Заголовок
        MutableComponent header = Component.literal("🎒 ").withStyle(COLOR_PRIMARY)
                .append(Component.literal("История контейнера ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(formatPos(pos)).withStyle(COLOR_TEXT))
                .append(Component.literal(" | ").withStyle(COLOR_SECONDARY))
                .append(Component.literal("Страница " + (page + 1) + "/" + totalPages).withStyle(COLOR_ACCENT));

        sendMessage(player, header);

        // 🔴 ИСПРАВЛЕНИЕ: аналогично для контейнеров
        int totalEntries = history.size();
        int start = Math.max(0, totalEntries - (page + 1) * PAGE_SIZE);
        int end = Math.min(totalEntries, totalEntries - page * PAGE_SIZE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        long now = System.currentTimeMillis();

        // 🔴 Выводим записи в обратном порядке (от новых к старым)
        for (int i = end - 1; i >= start; i--) {
            ItemLogEntry entry = history.get(i);
            sendItemEntry(player, entry, sdf, now);
        }

        sendNavigation(player, page, totalPages, "container");
    }

    private static void sendBlockEntry(ServerPlayer player, BlockLogEntry entry, SimpleDateFormat sdf, long now) {
        String time = sdf.format(new Date(entry.timestamp));
        String ago = formatTimeAgo(now - entry.timestamp);

        String action = switch(entry.actionType.toUpperCase()) {
            case "PLACE" -> "поставил";
            case "BREAK" -> "сломал";
            default -> entry.actionType.toLowerCase();
        };

        MutableComponent message = Component.literal("│ ").withStyle(COLOR_SECONDARY)
                .append(Component.literal(time).withStyle(COLOR_TIME))
                .append(Component.literal(" • ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(entry.playerName).withStyle(COLOR_ACCENT))
                .append(Component.literal(" " + action + " ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(entry.blockType).withStyle(COLOR_TEXT))
                .append(Component.literal(" (" + ago + ")").withStyle(COLOR_TIME));

        sendMessage(player, message);
    }

    private static void sendItemEntry(ServerPlayer player, ItemLogEntry entry, SimpleDateFormat sdf, long now) {
        String time = sdf.format(new Date(entry.timestamp));
        String ago = formatTimeAgo(now - entry.timestamp);

        String action = switch(entry.actionType.toUpperCase()) {
            case "PLACE", "DEPOSIT" -> "положил";
            case "TAKE", "WITHDRAW" -> "забрал";
            default -> entry.actionType.toLowerCase();
        };

        String actionSymbol = action.equals("положил") ? "⬆" : "⬇";
        ChatFormatting actionColor = action.equals("положил") ? ChatFormatting.GREEN : ChatFormatting.RED;

        MutableComponent message = Component.literal("│ ").withStyle(COLOR_SECONDARY)
                .append(Component.literal(time).withStyle(COLOR_TIME))
                .append(Component.literal(" • ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(entry.playerName).withStyle(COLOR_ACCENT))
                .append(Component.literal(" " + action + " ").withStyle(COLOR_SECONDARY))
                .append(Component.literal(entry.itemName).withStyle(COLOR_TEXT))
                .append(Component.literal(" ×" + entry.amount).withStyle(actionColor))
                .append(Component.literal(" " + actionSymbol).withStyle(actionColor))
                .append(Component.literal(" (" + ago + ")").withStyle(COLOR_TIME));

        sendMessage(player, message);
    }

    private static void sendNavigation(ServerPlayer player, int page, int totalPages, String type) {
        if (totalPages <= 1) return;

        MutableComponent navigation = Component.literal("└── ").withStyle(COLOR_SECONDARY);

        // Предыдущая страница
        if (page > 0) {
            // 🔴 ИСПРАВЛЕНИЕ: меняем команду с "/inspect_page X" на "/inspect_page prev"
            navigation.append(createNavigationButton("◀ Назад", "/inspect_page prev", "Предыдущая страница", COLOR_PRIMARY));
            navigation.append(Component.literal(" ").withStyle(COLOR_SECONDARY));
        }

        // Сброс к первой странице
        // 🔴 ИСПРАВЛЕНИЕ: меняем команду с "/inspect_page 0" на "/inspect_page reset"
        navigation.append(createNavigationButton("1", "/inspect_page reset", "Первая страница", COLOR_ACCENT));
        navigation.append(Component.literal(" ").withStyle(COLOR_SECONDARY));

        // Следующая страница
        if (page < totalPages - 1) {
            // 🔴 ИСПРАВЛЕНИЕ: меняем команду с "/inspect_page X" на "/inspect_page next"
            navigation.append(createNavigationButton("Вперёд ▶", "/inspect_page next", "Следующая страница", COLOR_PRIMARY));
        }

        sendMessage(player, navigation);
    }

    private static MutableComponent createNavigationButton(String text, String command, String hover, ChatFormatting color) {
        return Component.literal("[" + text + "]")
                .withStyle(style -> style
                        .withColor(color)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal(hover).withStyle(COLOR_SECONDARY)))
                );
    }

    private static String formatTimeAgo(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "с";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "м";
        long hours = minutes / 60;
        if (hours < 24) return hours + "ч";
        long days = hours / 24;
        return days + "д";
    }

    private static String formatPos(BlockPos pos) {
        return String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
    }

    private static void sendMessage(ServerPlayer player, Component message) {
        player.sendSystemMessage(message);
    }
}
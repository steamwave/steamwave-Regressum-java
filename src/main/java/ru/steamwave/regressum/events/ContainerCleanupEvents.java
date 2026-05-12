package ru.steamwave.regressum.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.IItemHandler;
import ru.steamwave.regressum.db.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContainerCleanupEvents {

    private final DbManager dbManager;

    public ContainerCleanupEvents(DbManager dbManager) {
        this.dbManager = new DbManager(); // 🔴 Инициализируем DbManager
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos pos = event.getPos();

        // Проверяем, есть ли у блока ItemHandler (контейнер)
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null) return;

        // 🔴 Если это контейнер - удаляем его историю после разрушения
        level.getServer().execute(() -> {
            try {
                // Ждем немного, чтобы блок полностью разрушился
                Thread.sleep(100);
                cleanupContainerData(level, pos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    // 🔴 Метод для очистки данных контейнера
    private void cleanupContainerData(Level level, BlockPos pos) {
        String world = level.dimension().location().toString();

        // 🔴 Получаем все координаты мультиблочного хранилища
        List<BlockPos> containerPositions = getMultiBlockContainerPositions(level, pos);

        // 🔴 Удаляем данные из базы для всех координат
        deleteContainerLogs(world, containerPositions);

        System.out.println("🗑️ Удалены логи контейнера для " + containerPositions.size() + " позиций в мире " + world);
    }

    // 🔴 Метод для получения всех позиций мультиблочного контейнера
    private List<BlockPos> getMultiBlockContainerPositions(Level level, BlockPos startPos) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(startPos); // Всегда добавляем начальную позицию

        // 🔴 Проверяем соседние блоки на наличие того же контейнера
        BlockPos[] neighbors = {
                startPos.north(), startPos.south(), startPos.east(), startPos.west(),
                startPos.above(), startPos.below()
        };

        for (BlockPos neighbor : neighbors) {
            // Проверяем, есть ли у соседнего блока ItemHandler
            IItemHandler neighborHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbor, null);
            if (neighborHandler != null) {
                positions.add(neighbor);
            }
        }

        return positions;
    }

    // 🔴 Метод для удаления логов контейнера из базы данных
    private void deleteContainerLogs(String world, List<BlockPos> positions) {
        if (positions.isEmpty()) return;

        // 🔴 Создаем условие WHERE для всех координат
        StringBuilder whereClause = new StringBuilder("WHERE world = ? AND (");
        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) whereClause.append(" OR ");
            whereClause.append("(x = ? AND y = ? AND z = ?)");
        }
        whereClause.append(")");

        String sql = "DELETE FROM item_logs " + whereClause.toString(); // 🔴 Исправлено на item_logs

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Устанавливаем параметры
            stmt.setString(1, world);

            int paramIndex = 2;
            for (BlockPos pos : positions) {
                stmt.setInt(paramIndex++, pos.getX());
                stmt.setInt(paramIndex++, pos.getY());
                stmt.setInt(paramIndex++, pos.getZ());
            }

            int deletedRows = stmt.executeUpdate();
            System.out.println("✅ Удалено " + deletedRows + " записей логов контейнера");

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при удалении логов контейнера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
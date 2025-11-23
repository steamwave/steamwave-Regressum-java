package ru.steamwave.regressum.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.steamwave.regressum.db.DbManager;
import ru.steamwave.regressum.model.BlockLogAction;
import ru.steamwave.regressum.model.ContainerLogAction;
import ru.steamwave.regressum.model.EntityLogAction;
import ru.steamwave.regressum.model.ItemLogAction;
import ru.steamwave.regressum.model.LogAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsyncDbWriter extends Thread {

    private static final Logger LOGGER = LogManager.getLogger(AsyncDbWriter.class);

    private final DbManager dbManager;
    private final int BATCH_SIZE = 100;
    private final long FLUSH_INTERVAL = 1000;
    private boolean running = true;

    public AsyncDbWriter(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        List<LogAction> batch = new ArrayList<>();
        long lastFlush = System.currentTimeMillis();

        while (running) {
            try {
                LogAction action = ActionQueue.queue.poll();
                if (action != null) batch.add(action);

                long now = System.currentTimeMillis();
                if (batch.size() >= BATCH_SIZE || (now - lastFlush >= FLUSH_INTERVAL && !batch.isEmpty())) {
                    flushBatch(batch);
                    batch.clear();
                    lastFlush = now;
                }

                if (action == null) Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void flushBatch(List<LogAction> batch) {
        String blockSQL = "INSERT INTO block_logs " +
                "(player_uuid, player_name, block, x, y, z, world, action_time, action_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String entitySQL = "INSERT INTO entity_logs " +
                "(killer_uuid, killer_name, killer_type, victim_type, victim_name, x, y, z, world, action_time, action_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSQL = "INSERT INTO item_logs " +
                "(player_uuid, player_name, item_name, x, y, z, world, action_time, action_type, amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String containerSQL = "INSERT INTO container_logs " +
                "(player_uuid, player_name, item_name, x, y, z, world, action_time, action_type, amount, container_type, container_data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection()) {
            // Блоки
            try (PreparedStatement stmt = conn.prepareStatement(blockSQL)) {
                int count = 0;
                for (LogAction action : batch) {
                    if (action instanceof BlockLogAction block) {
                        stmt.setObject(1, block.playerUuid);
                        stmt.setString(2, block.playerName);
                        stmt.setString(3, block.blockType);
                        stmt.setInt(4, block.x);
                        stmt.setInt(5, block.y);
                        stmt.setInt(6, block.z);
                        stmt.setString(7, block.world);
                        stmt.setLong(8, block.time);
                        stmt.setString(9, block.actionType);
                        stmt.addBatch();
                        count++;
                    }
                }
                if (count > 0) {
                    LOGGER.info("Executing block batch with {} actions", count);
                    stmt.executeBatch();
                }
            }

            // Сущности
            try (PreparedStatement stmt = conn.prepareStatement(entitySQL)) {
                int count = 0;
                for (LogAction action : batch) {
                    if (action instanceof EntityLogAction entity) {
                        stmt.setObject(1, entity.killerUUID);
                        stmt.setString(2, entity.killerName);
                        stmt.setString(3, entity.killerType);
                        stmt.setString(4, entity.victimType);
                        stmt.setString(5, entity.victimName);
                        stmt.setInt(6, entity.x);
                        stmt.setInt(7, entity.y);
                        stmt.setInt(8, entity.z);
                        stmt.setString(9, entity.world);
                        stmt.setLong(10, entity.time);
                        stmt.setString(11, entity.actionType);
                        stmt.addBatch();
                        count++;
                    }
                }
                if (count > 0) {
                    stmt.executeBatch();
                }
            }

            // Предметы
            try (PreparedStatement stmt = conn.prepareStatement(itemSQL)) {
                int count = 0;
                for (LogAction action : batch) {
                    if (action instanceof ItemLogAction item) {
                        stmt.setObject(1, item.playerUuid);
                        stmt.setString(2, item.playerName);
                        stmt.setString(3, item.itemName);
                        stmt.setInt(4, item.x);
                        stmt.setInt(5, item.y);
                        stmt.setInt(6, item.z);
                        stmt.setString(7, item.world);
                        stmt.setLong(8, item.time);
                        stmt.setString(9, item.actionType);
                        stmt.setInt(10, item.amount);
                        stmt.addBatch();
                        count++;
                    }
                }
                if (count > 0) {
                    LOGGER.info("Executing item batch with {} actions", count);
                    stmt.executeBatch();
                }
            }

            // Контейнеры
            try (PreparedStatement stmt = conn.prepareStatement(containerSQL)) {
                int count = 0;
                for (LogAction action : batch) {
                    if (action instanceof ContainerLogAction container) {
                        stmt.setObject(1, container.playerUuid);
                        stmt.setString(2, container.playerName);
                        stmt.setString(3, container.itemName);
                        stmt.setInt(4, container.x);
                        stmt.setInt(5, container.y);
                        stmt.setInt(6, container.z);
                        stmt.setString(7, container.world);
                        stmt.setLong(8, container.time);
                        stmt.setString(9, container.actionType);
                        stmt.setInt(10, container.amount);
                        stmt.setString(11, container.containerType);
                        stmt.setString(12, container.containerData);
                        stmt.addBatch();
                        count++;
                    }
                }
                if (count > 0) {
                    LOGGER.info("Executing container batch with {} actions", count);
                    stmt.executeBatch();
                }
            }

            LOGGER.info("[AsyncDbWriter] ✅ Flushed batch of {} actions", batch.size());

        } catch (SQLException e) {
            LOGGER.error("SQL error in flushBatch: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
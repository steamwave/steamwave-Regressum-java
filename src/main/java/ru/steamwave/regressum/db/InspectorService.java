package ru.steamwave.regressum.db;

import ru.steamwave.regressum.storage.BlockLogEntry;
import ru.steamwave.regressum.storage.ItemLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class InspectorService {



    private final DbManager dbManager;

    public InspectorService(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    // Получаем всю историю блоков без ограничений
    public List<BlockLogEntry> getAllBlockHistory(String world, int x, int y, int z) {
        String sql = "SELECT * FROM block_logs WHERE world=? AND x=? AND y=? AND z=? ORDER BY action_time ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setInt(4, z);

            ResultSet rs = stmt.executeQuery();
            List<BlockLogEntry> history = new ArrayList<>();
            while (rs.next()) {
                history.add(new BlockLogEntry(
                        rs.getLong("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("block"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getLong("action_time"),
                        rs.getString("action_type")
                ));
            }
            return history;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Получаем всю историю предметов (контейнеры)
    public List<ItemLogEntry> getAllItemHistory(String world, int x, int y, int z) {
        String sql = "SELECT * FROM item_logs WHERE world=? AND x=? AND y=? AND z=? ORDER BY action_time ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, x);
            stmt.setInt(3, y);
            stmt.setInt(4, z);

            ResultSet rs = stmt.executeQuery();
            List<ItemLogEntry> history = new ArrayList<>();
            while (rs.next()) {
                history.add(new ItemLogEntry(
                        rs.getLong("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("player_name"),
                        rs.getString("item_name"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getLong("action_time"),
                        rs.getInt("amount"),
                        rs.getString("action_type")
                ));
            }
            return history;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

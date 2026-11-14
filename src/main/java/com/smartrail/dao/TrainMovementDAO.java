package com.smartrail.dao;

import com.smartrail.model.TrainMovement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainMovementDAO {
    private static final Logger LOGGER = Logger.getLogger(TrainMovementDAO.class.getName());

    public List<TrainMovement> getRecentMovements(int limit) {
        List<TrainMovement> list = new ArrayList<>();
        String sql = "SELECT id, train_number, station_code, track_number, speed_kmh, " +
                     "progress_percent, movement_status, timestamp, notes " +
                     "FROM train_movements ORDER BY id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit <= 0 ? 50 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TrainMovement m = new TrainMovement(
                            rs.getInt("id"),
                            rs.getString("train_number"),
                            rs.getString("station_code"),
                            rs.getInt("track_number"),
                            rs.getInt("speed_kmh"),
                            rs.getDouble("progress_percent"),
                            rs.getString("movement_status"),
                            rs.getString("timestamp"),
                            rs.getString("notes")
                    );
                    list.add(m);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getRecentMovements: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean recordMovement(String trainNumber, String stationCode, int trackNumber, int speedKmh, double progress, String status, String notes) {
        String sql = "INSERT INTO train_movements (train_number, station_code, track_number, speed_kmh, progress_percent, movement_status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setString(2, stationCode);
            ps.setInt(3, trackNumber);
            ps.setInt(4, speedKmh);
            ps.setDouble(5, progress);
            ps.setString(6, status);
            ps.setString(7, notes != null ? notes : "");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording train movement: " + e.getMessage());
            return false;
        }
    }
}


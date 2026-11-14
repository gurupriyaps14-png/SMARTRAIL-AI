package com.smartrail.dao;

import com.smartrail.model.TrainDelay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainDelayDAO {
    private static final Logger LOGGER = Logger.getLogger(TrainDelayDAO.class.getName());

    public List<TrainDelay> getAllDelays() {
        List<TrainDelay> list = new ArrayList<>();
        String sql = "SELECT id, train_number, station_code, delay_minutes, delay_reason, recorded_at " +
                     "FROM train_delays ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TrainDelay d = new TrainDelay(
                        rs.getInt("id"),
                        rs.getString("train_number"),
                        rs.getString("station_code"),
                        rs.getInt("delay_minutes"),
                        rs.getString("delay_reason"),
                        rs.getString("recorded_at")
                );
                list.add(d);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getAllDelays: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean recordDelay(String trainNumber, String stationCode, int delayMinutes, String reason) {
        String sql = "INSERT INTO train_delays (train_number, station_code, delay_minutes, delay_reason) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            ps.setString(2, stationCode);
            ps.setInt(3, delayMinutes);
            ps.setString(4, reason != null ? reason : "Operational Delay");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording delay: " + e.getMessage());
            return false;
        }
    }
}


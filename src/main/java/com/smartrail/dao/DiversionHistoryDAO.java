package com.smartrail.dao;

import com.smartrail.model.DiversionRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiversionHistoryDAO {
    private static final Logger LOGGER = Logger.getLogger(DiversionHistoryDAO.class.getName());

    public List<DiversionRecord> getAllDiversions() {
        List<DiversionRecord> list = new ArrayList<>();
        String sql = "SELECT id, conflict_code, train_number, diverted_from_track, " +
                     "diverted_to_track, station_code, action_type, controller_approved, " +
                     "reason, diversion_time FROM diversion_history ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DiversionRecord d = new DiversionRecord(
                        rs.getInt("id"),
                        rs.getString("conflict_code"),
                        rs.getString("train_number"),
                        rs.getInt("diverted_from_track"),
                        rs.getInt("diverted_to_track"),
                        rs.getString("station_code"),
                        rs.getString("action_type"),
                        rs.getBoolean("controller_approved"),
                        rs.getString("reason"),
                        rs.getString("diversion_time")
                );
                list.add(d);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getAllDiversions: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean recordDiversion(String conflictCode, String trainNumber, int fromTrack, int toTrack,
                                   String stationCode, String actionType, boolean approved, String reason) {
        String sql = "INSERT INTO diversion_history (conflict_code, train_number, diverted_from_track, " +
                     "diverted_to_track, station_code, action_type, controller_approved, reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conflictCode);
            ps.setString(2, trainNumber);
            ps.setInt(3, fromTrack);
            ps.setInt(4, toTrack);
            ps.setString(5, stationCode);
            ps.setString(6, actionType);
            ps.setBoolean(7, approved);
            ps.setString(8, reason);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording diversion: " + e.getMessage());
            return false;
        }
    }
}


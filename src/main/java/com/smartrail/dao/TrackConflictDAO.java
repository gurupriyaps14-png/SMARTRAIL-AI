package com.smartrail.dao;

import com.smartrail.model.TrackConflictRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackConflictDAO {
    private static final Logger LOGGER = Logger.getLogger(TrackConflictDAO.class.getName());

    public List<TrackConflictRecord> getAllConflicts() {
        List<TrackConflictRecord> list = new ArrayList<>();
        String sql = "SELECT id, conflict_code, station_code, track_number, train_primary, " +
                     "train_secondary, conflict_type, severity, priority_winner, decision_reason, " +
                     "detected_time, status FROM track_conflicts ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TrackConflictRecord c = new TrackConflictRecord(
                        rs.getInt("id"),
                        rs.getString("conflict_code"),
                        rs.getString("station_code"),
                        rs.getInt("track_number"),
                        rs.getString("train_primary"),
                        rs.getString("train_secondary"),
                        rs.getString("conflict_type"),
                        rs.getString("severity"),
                        rs.getString("priority_winner"),
                        rs.getString("decision_reason"),
                        rs.getString("detected_time"),
                        rs.getString("status")
                );
                list.add(c);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getAllConflicts: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean recordConflict(String conflictCode, String stationCode, int trackNumber,
                                  String primaryTrain, String secondaryTrain, String conflictType,
                                  String severity, String winner, String reason) {
        String sql = "INSERT INTO track_conflicts (conflict_code, station_code, track_number, train_primary, " +
                     "train_secondary, conflict_type, severity, priority_winner, decision_reason, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, conflictCode);
            ps.setString(2, stationCode);
            ps.setInt(3, trackNumber);
            ps.setString(4, primaryTrain);
            ps.setString(5, secondaryTrain);
            ps.setString(6, conflictType);
            ps.setString(7, severity);
            ps.setString(8, winner);
            ps.setString(9, reason);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error recording conflict: " + e.getMessage());
            return false;
        }
    }
}


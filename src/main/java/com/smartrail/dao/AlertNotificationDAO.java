package com.smartrail.dao;

import com.smartrail.model.AlertNotification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertNotificationDAO {
    private static final Logger LOGGER = Logger.getLogger(AlertNotificationDAO.class.getName());

    public List<AlertNotification> getAllAlerts() {
        List<AlertNotification> list = new ArrayList<>();
        String sql = "SELECT id, alert_code, train_number, conflict_type, time_detected, " +
                     "recommended_action, dispatch_status, severity, station_code, track_number, details " +
                     "FROM alert_notifications ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AlertNotification a = new AlertNotification(
                        rs.getInt("id"),
                        rs.getString("alert_code"),
                        rs.getString("train_number"),
                        rs.getString("conflict_type"),
                        rs.getString("time_detected"),
                        rs.getString("recommended_action"),
                        rs.getString("dispatch_status"),
                        rs.getString("severity"),
                        rs.getString("station_code"),
                        rs.getInt("track_number"),
                        rs.getString("details")
                );
                list.add(a);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getAllAlerts: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean insertAlert(String alertCode, String trainNumber, String conflictType,
                               String recommendedAction, String dispatchStatus, String severity,
                               String stationCode, int trackNumber, String details) {
        String sql = "INSERT INTO alert_notifications (alert_code, train_number, conflict_type, " +
                     "recommended_action, dispatch_status, severity, station_code, track_number, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, alertCode);
            ps.setString(2, trainNumber);
            ps.setString(3, conflictType);
            ps.setString(4, recommendedAction);
            ps.setString(5, dispatchStatus);
            ps.setString(6, severity);
            ps.setString(7, stationCode);
            ps.setInt(8, trackNumber);
            ps.setString(9, details);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error inserting alert notification: " + e.getMessage());
            return false;
        }
    }
}


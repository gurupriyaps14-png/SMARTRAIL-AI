package com.smartrail.dao;

import com.smartrail.model.TrainSchedule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainScheduleDAO {
    private static final Logger LOGGER = Logger.getLogger(TrainScheduleDAO.class.getName());

    public List<TrainSchedule> getAllSchedules() {
        List<TrainSchedule> list = new ArrayList<>();
        String sql = "SELECT ts.id, ts.train_id, t.train_number, t.train_name, " +
                     "ts.station_id, s.code AS station_code, s.name AS station_name, " +
                     "ts.scheduled_arrival, ts.scheduled_departure, ts.platform_number, " +
                     "ts.stop_sequence, ts.halt_duration_minutes " +
                     "FROM train_schedules ts " +
                     "LEFT JOIN trains t ON ts.train_id = t.id " +
                     "LEFT JOIN stations s ON ts.station_id = s.id " +
                     "ORDER BY ts.train_id, ts.stop_sequence";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TrainSchedule s = new TrainSchedule(
                        rs.getInt("id"),
                        rs.getInt("train_id"),
                        rs.getString("train_number") != null ? rs.getString("train_number") : "TR-" + rs.getInt("train_id"),
                        rs.getString("train_name") != null ? rs.getString("train_name") : "Scheduled Express",
                        rs.getInt("station_id"),
                        rs.getString("station_code") != null ? rs.getString("station_code") : "STN",
                        rs.getString("station_name") != null ? rs.getString("station_name") : "Station",
                        rs.getString("scheduled_arrival"),
                        rs.getString("scheduled_departure"),
                        rs.getInt("platform_number"),
                        rs.getInt("stop_sequence"),
                        rs.getInt("halt_duration_minutes")
                );
                list.add(s);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getAllSchedules: " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateSchedule(int scheduleId, String arrival, String departure, int platform) {
        String sql = "UPDATE train_schedules SET scheduled_arrival = ?, scheduled_departure = ?, platform_number = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, arrival);
            ps.setString(2, departure);
            ps.setInt(3, platform);
            ps.setInt(4, scheduleId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in updateSchedule: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean insertSchedule(int trainId, int stationId, String arrival, String departure, int platform, int sequence) {
        String sql = "INSERT INTO train_schedules (train_id, station_id, scheduled_arrival, scheduled_departure, platform_number, stop_sequence, halt_duration_minutes) VALUES (?, ?, ?, ?, ?, ?, 5)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainId);
            ps.setInt(2, stationId);
            ps.setString(3, arrival);
            ps.setString(4, departure);
            ps.setInt(5, platform);
            ps.setInt(6, sequence);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in insertSchedule: " + e.getMessage(), e);
            return false;
        }
    }
}


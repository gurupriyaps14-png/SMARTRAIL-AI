package com.smartrail.dao;

import com.smartrail.model.Train;
import com.smartrail.model.TrainCategory;
import com.smartrail.model.TrainStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Trains operating across Tamil Nadu divisions.
 */
public class TrainDAO {
    private static final Logger LOGGER = Logger.getLogger(TrainDAO.class.getName());

    private static final String BASE_SELECT =
            "SELECT t.id, t.train_number, t.train_name, t.category, " +
            "t.origin_station_id, s_orig.name AS origin_name, " +
            "t.destination_station_id, s_dest.name AS dest_name, " +
            "t.current_station_id, s_curr.name AS curr_name, " +
            "s_curr.division_id, d.code AS division_code, " +
            "t.current_track_number, t.direction, t.status, " +
            "t.eta, t.etd, t.delay_minutes, t.speed_kmh, " +
            "t.progress_percent, t.time_slot, t.is_simulated " +
            "FROM trains t " +
            "JOIN stations s_orig ON t.origin_station_id = s_orig.id " +
            "JOIN stations s_dest ON t.destination_station_id = s_dest.id " +
            "JOIN stations s_curr ON t.current_station_id = s_curr.id " +
            "JOIN divisions d ON s_curr.division_id = d.id ";

    public List<Train> getAllTrains() {
        List<Train> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTrain(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all trains: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Train> getTrainsByStationId(int stationId) {
        List<Train> list = new ArrayList<>();
        String sql = BASE_SELECT +
                     "WHERE t.current_station_id = ? OR t.origin_station_id = ? OR t.destination_station_id = ? " +
                     "ORDER BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            ps.setInt(2, stationId);
            ps.setInt(3, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrain(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching trains for stationId " + stationId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public List<Train> getTrainsByDivisionCode(String divisionCode) {
        List<Train> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE UPPER(d.code) = UPPER(?) ORDER BY t.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, divisionCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrain(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching trains for division " + divisionCode + ": " + e.getMessage(), e);
        }
        return list;
    }

    public List<Train> getTrainsByFilter(String divisionCode, Integer stationId, String timeSlot, String category) {
        List<Train> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (stationId != null && stationId > 0) {
            sql.append("AND (t.current_station_id = ? OR t.origin_station_id = ? OR t.destination_station_id = ?) ");
            params.add(stationId);
            params.add(stationId);
            params.add(stationId);
        } else if (divisionCode != null && !divisionCode.trim().isEmpty() && !"ALL".equalsIgnoreCase(divisionCode)) {
            sql.append("AND UPPER(d.code) = UPPER(?) ");
            params.add(divisionCode.trim());
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append("AND UPPER(t.category) = UPPER(?) ");
            params.add(category.trim());
        }

        if (timeSlot != null && !timeSlot.trim().isEmpty() && !"ALL".equalsIgnoreCase(timeSlot)) {
            sql.append("AND (UPPER(t.time_slot) = UPPER(?) OR UPPER(t.time_slot) = 'ALL') ");
            params.add(timeSlot.trim());
        }

        sql.append("ORDER BY t.id");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrain(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error querying trains with filter: " + e.getMessage(), e);
        }
        return list;
    }

    public Train getTrainByNumber(String trainNumber) {
        String sql = BASE_SELECT + "WHERE t.train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrain(rs);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching train " + trainNumber + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean updateTrainProgress(String trainNumber, double progress, String status, int trackNumber) {
        String sql = "UPDATE trains SET progress_percent = ?, status = ?, current_track_number = ? WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, progress);
            ps.setString(2, status);
            ps.setInt(3, trackNumber);
            ps.setString(4, trainNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating train progress: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateTrainDelay(String trainNumber, int delayMinutes, String status) {
        String sql = "UPDATE trains SET delay_minutes = ?, status = ? WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delayMinutes);
            ps.setString(2, status);
            ps.setString(3, trainNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating train delay: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateTrainDetails(String trainNumber, String trainName, String category, int speedKmh, String eta, int delayMinutes) {
        String sql = "UPDATE trains SET train_name = ?, category = ?, speed_kmh = ?, eta = ?, delay_minutes = ? WHERE train_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trainName);
            ps.setString(2, category);
            ps.setInt(3, speedKmh);
            ps.setString(4, eta);
            ps.setInt(5, delayMinutes);
            ps.setString(6, trainNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating train details: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean addSimulatedTrain(Train train) {
        String sql = "INSERT INTO trains (id, train_number, train_name, category, origin_station_id, " +
                "destination_station_id, current_station_id, current_track_number, direction, status, " +
                "eta, etd, delay_minutes, speed_kmh, progress_percent, time_slot, is_simulated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int nextId = (int) (System.currentTimeMillis() % 100000);
            ps.setInt(1, nextId);
            ps.setString(2, train.getTrainNumber());
            ps.setString(3, train.getTrainName());
            ps.setString(4, train.getCategory().name());
            ps.setInt(5, train.getOriginStationId());
            ps.setInt(6, train.getDestinationStationId());
            ps.setInt(7, train.getCurrentStationId());
            ps.setInt(8, train.getCurrentTrackNumber());
            ps.setString(9, train.getDirection());
            ps.setString(10, train.getStatus().name());
            ps.setString(11, train.getEta());
            ps.setString(12, train.getEtd());
            ps.setInt(13, train.getDelayMinutes());
            ps.setInt(14, train.getSpeedKmh());
            ps.setDouble(15, train.getProgressPercent());
            ps.setString(16, train.getTimeSlot() != null ? train.getTimeSlot() : "ALL");
            ps.setBoolean(17, true);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding simulated train: " + e.getMessage(), e);
            return false;
        }
    }

    private Train mapTrain(ResultSet rs) throws Exception {
        Train t = new Train();
        t.setId(rs.getInt("id"));
        t.setTrainNumber(rs.getString("train_number"));
        t.setTrainName(rs.getString("train_name"));
        t.setCategory(TrainCategory.fromString(rs.getString("category")));
        t.setOriginStationId(rs.getInt("origin_station_id"));
        t.setOriginStationName(rs.getString("origin_name"));
        t.setDestinationStationId(rs.getInt("destination_station_id"));
        t.setDestinationStationName(rs.getString("dest_name"));
        t.setCurrentStationId(rs.getInt("current_station_id"));
        t.setCurrentStationName(rs.getString("curr_name"));
        t.setCurrentTrackNumber(rs.getInt("current_track_number"));
        t.setDirection(rs.getString("direction"));
        t.setStatus(TrainStatus.fromString(rs.getString("status")));
        t.setEta(rs.getString("eta"));
        t.setEtd(rs.getString("etd"));
        t.setDelayMinutes(rs.getInt("delay_minutes"));
        t.setSpeedKmh(rs.getInt("speed_kmh"));
        t.setProgressPercent(rs.getDouble("progress_percent"));
        t.setTimeSlot(rs.getString("time_slot"));
        t.setSimulated(rs.getBoolean("is_simulated"));

        // Determine intermediate next station name
        if (t.getDestinationStationName() != null) {
            t.setNextStationName(t.getDestinationStationName());
        } else {
            t.setNextStationName("Next Block Section");
        }

        return t;
    }
}


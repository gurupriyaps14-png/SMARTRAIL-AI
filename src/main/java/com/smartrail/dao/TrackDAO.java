package com.smartrail.dao;

import com.smartrail.model.Track;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Railway Tracks and Platform Lines.
 */
public class TrackDAO {
    private static final Logger LOGGER = Logger.getLogger(TrackDAO.class.getName());

    public List<Track> getTracksByStationId(int stationId) {
        List<Track> list = new ArrayList<>();
        String sql = "SELECT t.id, t.station_id, t.track_number, t.track_name, t.track_type, " +
                     "t.max_speed_kmh, t.status, t.length_meters, t.current_train_number, " +
                     "tr.train_name as current_train_name " +
                     "FROM tracks t " +
                     "LEFT JOIN trains tr ON t.current_train_number = tr.train_number " +
                     "WHERE t.station_id = ? ORDER BY t.track_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Track track = new Track(
                            rs.getInt("id"),
                            rs.getInt("station_id"),
                            rs.getInt("track_number"),
                            rs.getString("track_name"),
                            rs.getString("track_type"),
                            rs.getInt("max_speed_kmh"),
                            rs.getString("status"),
                            rs.getInt("length_meters"),
                            rs.getString("current_train_number")
                    );
                    track.setCurrentTrainName(rs.getString("current_train_name"));
                    list.add(track);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching tracks for stationId " + stationId + ": " + e.getMessage(), e);
        }

        // If no specific tracks exist in DB for this station, generate standard 5 control tracks
        if (list.isEmpty()) {
            list = generateDefaultStationTracks(stationId);
        }
        return list;
    }

    public List<Track> generateDefaultStationTracks(int stationId) {
        List<Track> tracks = new ArrayList<>();
        tracks.add(new Track(stationId * 10 + 1, stationId, 1, "Track 1 - Up Main Line", "MAIN_UP", 110, "CLEAR", 750, null));
        tracks.add(new Track(stationId * 10 + 2, stationId, 2, "Track 2 - Down Main Line", "MAIN_DOWN", 110, "CLEAR", 750, null));
        tracks.add(new Track(stationId * 10 + 3, stationId, 3, "Track 3 - Platform Loop 1", "LOOP_1", 65, "CLEAR", 700, null));
        tracks.add(new Track(stationId * 10 + 4, stationId, 4, "Track 4 - Platform Loop 2", "LOOP_2", 65, "CLEAR", 700, null));
        tracks.add(new Track(stationId * 10 + 5, stationId, 5, "Track 5 - Freight Bypass Siding", "FREIGHT_SIDING", 50, "CLEAR", 850, null));
        return tracks;
    }

    public boolean updateTrackStatus(int stationId, int trackNumber, String status, String trainNumber) {
        String sql = "UPDATE tracks SET status = ?, current_train_number = ? WHERE station_id = ? AND track_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, trainNumber);
            ps.setInt(3, stationId);
            ps.setInt(4, trackNumber);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating track status: " + e.getMessage(), e);
            return false;
        }
    }
}


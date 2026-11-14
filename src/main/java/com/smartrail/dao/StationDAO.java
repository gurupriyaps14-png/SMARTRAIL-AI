package com.smartrail.dao;

import com.smartrail.model.Station;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Railway Stations in Tamil Nadu.
 */
public class StationDAO {
    private static final Logger LOGGER = Logger.getLogger(StationDAO.class.getName());

    public List<Station> getAllStations() {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT s.id, s.code, s.name, s.division_id, d.code as division_code, " +
                     "s.platforms_count, s.tracks_count, s.junction_type, s.latitude, s.longitude, s.is_major_junction " +
                     "FROM stations s JOIN divisions d ON s.division_id = d.id ORDER BY s.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStation(rs));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all stations: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Station> getStationsByDivisionCode(String divisionCode) {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT s.id, s.code, s.name, s.division_id, d.code as division_code, " +
                     "s.platforms_count, s.tracks_count, s.junction_type, s.latitude, s.longitude, s.is_major_junction " +
                     "FROM stations s JOIN divisions d ON s.division_id = d.id WHERE UPPER(d.code) = UPPER(?) ORDER BY s.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, divisionCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStation(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching stations for division " + divisionCode + ": " + e.getMessage(), e);
        }
        return list;
    }

    public List<Station> getStationsByDivisionId(int divisionId) {
        List<Station> list = new ArrayList<>();
        String sql = "SELECT s.id, s.code, s.name, s.division_id, d.code as division_code, " +
                     "s.platforms_count, s.tracks_count, s.junction_type, s.latitude, s.longitude, s.is_major_junction " +
                     "FROM stations s JOIN divisions d ON s.division_id = d.id WHERE s.division_id = ? ORDER BY s.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, divisionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStation(rs));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching stations by division id " + divisionId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public Station getStationById(int id) {
        String sql = "SELECT s.id, s.code, s.name, s.division_id, d.code as division_code, " +
                     "s.platforms_count, s.tracks_count, s.junction_type, s.latitude, s.longitude, s.is_major_junction " +
                     "FROM stations s JOIN divisions d ON s.division_id = d.id WHERE s.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStation(rs);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching station by id " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public Station getStationByCode(String code) {
        String sql = "SELECT s.id, s.code, s.name, s.division_id, d.code as division_code, " +
                     "s.platforms_count, s.tracks_count, s.junction_type, s.latitude, s.longitude, s.is_major_junction " +
                     "FROM stations s JOIN divisions d ON s.division_id = d.id WHERE UPPER(s.code) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStation(rs);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching station by code " + code + ": " + e.getMessage(), e);
        }
        return null;
    }

    private Station mapStation(ResultSet rs) throws Exception {
        return new Station(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("division_id"),
                rs.getString("division_code"),
                rs.getInt("platforms_count"),
                rs.getInt("tracks_count"),
                rs.getString("junction_type"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getBoolean("is_major_junction")
        );
    }
}


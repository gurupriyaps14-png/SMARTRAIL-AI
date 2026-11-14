package com.smartrail.dao;

import com.smartrail.model.Division;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Railway Divisions in Tamil Nadu.
 */
public class DivisionDAO {
    private static final Logger LOGGER = Logger.getLogger(DivisionDAO.class.getName());

    public List<Division> getAllDivisions() {
        List<Division> list = new ArrayList<>();
        String sql = "SELECT id, code, name, headquarters, zone, active_trains_count, description FROM divisions ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Division d = new Division(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("headquarters"),
                        rs.getString("zone"),
                        rs.getInt("active_trains_count"),
                        rs.getString("description")
                );
                list.add(d);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching all divisions: " + e.getMessage(), e);
        }
        return list;
    }

    public Division getDivisionByCode(String code) {
        String sql = "SELECT id, code, name, headquarters, zone, active_trains_count, description FROM divisions WHERE code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Division(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("headquarters"),
                            rs.getString("zone"),
                            rs.getInt("active_trains_count"),
                            rs.getString("description")
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching division by code " + code + ": " + e.getMessage(), e);
        }
        return null;
    }

    public Division getDivisionById(int id) {
        String sql = "SELECT id, code, name, headquarters, zone, active_trains_count, description FROM divisions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Division(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("headquarters"),
                            rs.getString("zone"),
                            rs.getInt("active_trains_count"),
                            rs.getString("description")
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching division by id " + id + ": " + e.getMessage(), e);
        }
        return null;
    }
}


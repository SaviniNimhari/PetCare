package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.PetOwner;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetOwnerDAO {
    private static final Logger logger = LoggerFactory.getLogger(PetOwnerDAO.class);

    public List<PetOwner> getAllOwners() {
        List<PetOwner> owners = new ArrayList<>();
        String sql = "SELECT * FROM pet_owners ORDER BY first_name ASC, last_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                owners.add(mapResultSetToOwner(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching pet owners: {}", e.getMessage());
        }
        return owners;
    }

    public List<PetOwner> searchOwners(String keyword) {
        List<PetOwner> owners = new ArrayList<>();
        String sql = "SELECT * FROM pet_owners WHERE first_name LIKE ? OR last_name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY first_name ASC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    owners.add(mapResultSetToOwner(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching pet owners: {}", e.getMessage());
        }
        return owners;
    }

    public PetOwner getOwnerById(int id) {
        String sql = "SELECT * FROM pet_owners WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOwner(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching pet owner #{}: {}", id, e.getMessage());
        }
        return null;
    }

    public boolean createOwner(PetOwner owner) {
        String sql = "INSERT INTO pet_owners (user_id, first_name, last_name, email, phone, address, city, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (owner.getUserId() != null) {
                stmt.setInt(1, owner.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, owner.getFirstName());
            stmt.setString(3, owner.getLastName());
            stmt.setString(4, owner.getEmail());
            stmt.setString(5, owner.getPhone());
            stmt.setString(6, owner.getAddress());
            stmt.setString(7, owner.getCity());
            stmt.setString(8, owner.getEmergencyContact());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        owner.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating pet owner: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateOwner(PetOwner owner) {
        String sql = "UPDATE pet_owners SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, city = ?, emergency_contact = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, owner.getFirstName());
            stmt.setString(2, owner.getLastName());
            stmt.setString(3, owner.getEmail());
            stmt.setString(4, owner.getPhone());
            stmt.setString(5, owner.getAddress());
            stmt.setString(6, owner.getCity());
            stmt.setString(7, owner.getEmergencyContact());
            stmt.setInt(8, owner.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating pet owner #{}: {}", owner.getId(), e.getMessage());
        }
        return false;
    }

    public boolean deleteOwner(int id) {
        String sql = "DELETE FROM pet_owners WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting pet owner #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private PetOwner mapResultSetToOwner(ResultSet rs) throws SQLException {
        PetOwner owner = new PetOwner();
        owner.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        owner.setUserId(rs.wasNull() ? null : userId);
        owner.setFirstName(rs.getString("first_name"));
        owner.setLastName(rs.getString("last_name"));
        owner.setEmail(rs.getString("email"));
        owner.setPhone(rs.getString("phone"));
        owner.setAddress(rs.getString("address"));
        owner.setCity(rs.getString("city"));
        owner.setEmergencyContact(rs.getString("emergency_contact"));
        owner.setCreatedAt(rs.getTimestamp("created_at"));
        return owner;
    }
}

package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Veterinarian;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeterinarianDAO {
    private static final Logger logger = LoggerFactory.getLogger(VeterinarianDAO.class);

    public List<Veterinarian> getAllVeterinarians() {
        List<Veterinarian> vets = new ArrayList<>();
        String sql = "SELECT * FROM veterinarians ORDER BY first_name ASC, last_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vets.add(mapResultSetToVet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching veterinarians: {}", e.getMessage());
        }
        return vets;
    }

    public Veterinarian getVeterinarianById(int id) {
        String sql = "SELECT * FROM veterinarians WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching veterinarian #{}: {}", id, e.getMessage());
        }
        return null;
    }

    public boolean createVeterinarian(Veterinarian vet) {
        String sql = "INSERT INTO veterinarians (user_id, first_name, last_name, specialization, license_number, phone, email, available_days, consultation_fee) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (vet.getUserId() != null) {
                stmt.setInt(1, vet.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, vet.getFirstName());
            stmt.setString(3, vet.getLastName());
            stmt.setString(4, vet.getSpecialization());
            stmt.setString(5, vet.getLicenseNumber());
            stmt.setString(6, vet.getPhone());
            stmt.setString(7, vet.getEmail());
            stmt.setString(8, vet.getAvailableDays());
            stmt.setBigDecimal(9, vet.getConsultationFee());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        vet.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating veterinarian: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateVeterinarian(Veterinarian vet) {
        String sql = "UPDATE veterinarians SET first_name = ?, last_name = ?, specialization = ?, license_number = ?, phone = ?, email = ?, available_days = ?, consultation_fee = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, vet.getFirstName());
            stmt.setString(2, vet.getLastName());
            stmt.setString(3, vet.getSpecialization());
            stmt.setString(4, vet.getLicenseNumber());
            stmt.setString(5, vet.getPhone());
            stmt.setString(6, vet.getEmail());
            stmt.setString(7, vet.getAvailableDays());
            stmt.setBigDecimal(8, vet.getConsultationFee());
            stmt.setInt(9, vet.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating veterinarian #{}: {}", vet.getId(), e.getMessage());
        }
        return false;
    }

    public boolean deleteVeterinarian(int id) {
        String sql = "DELETE FROM veterinarians WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting veterinarian #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private Veterinarian mapResultSetToVet(ResultSet rs) throws SQLException {
        Veterinarian vet = new Veterinarian();
        vet.setId(rs.getInt("id"));
        int userId = rs.getInt("user_id");
        vet.setUserId(rs.wasNull() ? null : userId);
        vet.setFirstName(rs.getString("first_name"));
        vet.setLastName(rs.getString("last_name"));
        vet.setSpecialization(rs.getString("specialization"));
        vet.setLicenseNumber(rs.getString("license_number"));
        vet.setPhone(rs.getString("phone"));
        vet.setEmail(rs.getString("email"));
        vet.setAvailableDays(rs.getString("available_days"));
        vet.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        vet.setCreatedAt(rs.getTimestamp("created_at"));
        return vet;
    }
}

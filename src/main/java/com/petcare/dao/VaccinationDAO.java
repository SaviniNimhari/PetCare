package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Vaccination;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaccinationDAO {
    private static final Logger logger = LoggerFactory.getLogger(VaccinationDAO.class);

    public List<Vaccination> getVaccinationsByPetId(int petId) {
        List<Vaccination> list = new ArrayList<>();
        String sql = "SELECT v.*, p.name AS pet_name, CONCAT('Dr. ', vt.first_name, ' ', vt.last_name) AS vet_name " +
                     "FROM vaccinations v " +
                     "JOIN pets p ON v.pet_id = p.id " +
                     "LEFT JOIN veterinarians vt ON v.administered_by_vet_id = vt.id " +
                     "WHERE v.pet_id = ? " +
                     "ORDER BY v.administered_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVaccination(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching vaccinations for pet #{}: {}", petId, e.getMessage());
        }
        return list;
    }

    public List<Vaccination> getAllVaccinations() {
        List<Vaccination> list = new ArrayList<>();
        String sql = "SELECT v.*, p.name AS pet_name, CONCAT('Dr. ', vt.first_name, ' ', vt.last_name) AS vet_name " +
                     "FROM vaccinations v " +
                     "JOIN pets p ON v.pet_id = p.id " +
                     "LEFT JOIN veterinarians vt ON v.administered_by_vet_id = vt.id " +
                     "ORDER BY v.administered_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToVaccination(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all vaccinations: {}", e.getMessage());
        }
        return list;
    }

    public List<Vaccination> getUpcomingVaccinations() {
        List<Vaccination> list = new ArrayList<>();
        String sql = "SELECT v.*, p.name AS pet_name, CONCAT('Dr. ', vt.first_name, ' ', vt.last_name) AS vet_name " +
                     "FROM vaccinations v " +
                     "JOIN pets p ON v.pet_id = p.id " +
                     "LEFT JOIN veterinarians vt ON v.administered_by_vet_id = vt.id " +
                     "WHERE v.next_due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                     "ORDER BY v.next_due_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToVaccination(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching upcoming vaccinations: {}", e.getMessage());
        }
        return list;
    }

    public boolean createVaccination(Vaccination v) {
        String sql = "INSERT INTO vaccinations (pet_id, vaccine_name, administered_date, next_due_date, batch_number, administered_by_vet_id, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, v.getPetId());
            stmt.setString(2, v.getVaccineName());
            stmt.setDate(3, v.getAdministeredDate());
            stmt.setDate(4, v.getNextDueDate());
            stmt.setString(5, v.getBatchNumber());
            if (v.getAdministeredByVetId() != null) {
                stmt.setInt(6, v.getAdministeredByVetId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, v.getStatus());
            stmt.setString(8, v.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        v.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating vaccination: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateVaccination(Vaccination v) {
        String sql = "UPDATE vaccinations SET vaccine_name = ?, administered_date = ?, next_due_date = ?, batch_number = ?, administered_by_vet_id = ?, status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, v.getVaccineName());
            stmt.setDate(2, v.getAdministeredDate());
            stmt.setDate(3, v.getNextDueDate());
            stmt.setString(4, v.getBatchNumber());
            if (v.getAdministeredByVetId() != null) {
                stmt.setInt(5, v.getAdministeredByVetId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, v.getStatus());
            stmt.setString(7, v.getNotes());
            stmt.setInt(8, v.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating vaccination #{}: {}", v.getId(), e.getMessage());
        }
        return false;
    }

    public boolean deleteVaccination(int id) {
        String sql = "DELETE FROM vaccinations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting vaccination #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private Vaccination mapResultSetToVaccination(ResultSet rs) throws SQLException {
        Vaccination v = new Vaccination();
        v.setId(rs.getInt("id"));
        v.setPetId(rs.getInt("pet_id"));
        v.setPetName(rs.getString("pet_name"));
        v.setVaccineName(rs.getString("vaccine_name"));
        v.setAdministeredDate(rs.getDate("administered_date"));
        v.setNextDueDate(rs.getDate("next_due_date"));
        v.setBatchNumber(rs.getString("batch_number"));
        int vetId = rs.getInt("administered_by_vet_id");
        v.setAdministeredByVetId(rs.wasNull() ? null : vetId);
        v.setVetName(rs.getString("vet_name"));
        v.setStatus(rs.getString("status"));
        v.setNotes(rs.getString("notes"));
        v.setCreatedAt(rs.getTimestamp("created_at"));
        return v;
    }
}

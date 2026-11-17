package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppointmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentDAO.class);

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS pet_name, CONCAT(o.first_name, ' ', o.last_name) AS owner_name, " +
                     "CONCAT('Dr. ', v.first_name, ' ', v.last_name) AS vet_name " +
                     "FROM appointments a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN pet_owners o ON a.owner_id = o.id " +
                     "JOIN veterinarians v ON a.vet_id = v.id " +
                     "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching appointments: {}", e.getMessage());
        }
        return appointments;
    }

    public List<Appointment> getTodayAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, p.name AS pet_name, CONCAT(o.first_name, ' ', o.last_name) AS owner_name, " +
                     "CONCAT('Dr. ', v.first_name, ' ', v.last_name) AS vet_name " +
                     "FROM appointments a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN pet_owners o ON a.owner_id = o.id " +
                     "JOIN veterinarians v ON a.vet_id = v.id " +
                     "WHERE a.appointment_date = CURDATE() " +
                     "ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching today's appointments: {}", e.getMessage());
        }
        return appointments;
    }

    public boolean hasConflict(int vetId, Date date, Time time, int excludeAppointmentId) {
        String sql = "SELECT COUNT(*) FROM appointments " +
                     "WHERE vet_id = ? AND appointment_date = ? AND appointment_time = ? " +
                     "AND status != 'CANCELLED' AND id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, vetId);
            stmt.setDate(2, date);
            stmt.setTime(3, time);
            stmt.setInt(4, excludeAppointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking appointment conflict: {}", e.getMessage());
        }
        return false;
    }

    public boolean createAppointment(Appointment a) {
        String sql = "INSERT INTO appointments (pet_id, owner_id, vet_id, appointment_date, appointment_time, reason, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, a.getPetId());
            stmt.setInt(2, a.getOwnerId());
            stmt.setInt(3, a.getVetId());
            stmt.setDate(4, a.getAppointmentDate());
            stmt.setTime(5, a.getAppointmentTime());
            stmt.setString(6, a.getReason());
            stmt.setString(7, a.getStatus());
            stmt.setString(8, a.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        a.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating appointment: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateAppointment(Appointment a) {
        String sql = "UPDATE appointments SET pet_id = ?, owner_id = ?, vet_id = ?, appointment_date = ?, appointment_time = ?, reason = ?, status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, a.getPetId());
            stmt.setInt(2, a.getOwnerId());
            stmt.setInt(3, a.getVetId());
            stmt.setDate(4, a.getAppointmentDate());
            stmt.setTime(5, a.getAppointmentTime());
            stmt.setString(6, a.getReason());
            stmt.setString(7, a.getStatus());
            stmt.setString(8, a.getNotes());
            stmt.setInt(9, a.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating appointment #{}: {}", a.getId(), e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating appointment status #{}: {}", id, e.getMessage());
        }
        return false;
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting appointment #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPetId(rs.getInt("pet_id"));
        a.setOwnerId(rs.getInt("owner_id"));
        a.setVetId(rs.getInt("vet_id"));
        a.setPetName(rs.getString("pet_name"));
        a.setOwnerName(rs.getString("owner_name"));
        a.setVetName(rs.getString("vet_name"));
        a.setAppointmentDate(rs.getDate("appointment_date"));
        a.setAppointmentTime(rs.getTime("appointment_time"));
        a.setReason(rs.getString("reason"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}

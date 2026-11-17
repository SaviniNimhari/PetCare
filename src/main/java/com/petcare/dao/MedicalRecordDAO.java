package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.MedicalRecord;
import com.petcare.model.Prescription;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicalRecordDAO {
    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordDAO.class);

    public List<MedicalRecord> getRecordsByPetId(int petId) {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT m.*, p.name AS pet_name, CONCAT('Dr. ', v.first_name, ' ', v.last_name) AS vet_name " +
                     "FROM medical_records m " +
                     "JOIN pets p ON m.pet_id = p.id " +
                     "JOIN veterinarians v ON m.vet_id = v.id " +
                     "WHERE m.pet_id = ? " +
                     "ORDER BY m.visit_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, petId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalRecord record = mapResultSetToRecord(rs);
                    record.setPrescriptions(getPrescriptionsForRecord(conn, record.getId()));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching medical records for pet #{}: {}", petId, e.getMessage());
        }
        return records;
    }

    public List<MedicalRecord> getAllRecords() {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT m.*, p.name AS pet_name, CONCAT('Dr. ', v.first_name, ' ', v.last_name) AS vet_name " +
                     "FROM medical_records m " +
                     "JOIN pets p ON m.pet_id = p.id " +
                     "JOIN veterinarians v ON m.vet_id = v.id " +
                     "ORDER BY m.visit_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching medical records: {}", e.getMessage());
        }
        return records;
    }

    public boolean createRecord(MedicalRecord record) {
        String sql = "INSERT INTO medical_records (pet_id, vet_id, appointment_id, visit_date, symptoms, diagnosis, treatment, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, record.getPetId());
            stmt.setInt(2, record.getVetId());
            if (record.getAppointmentId() != null) {
                stmt.setInt(3, record.getAppointmentId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setDate(4, record.getVisitDate());
            stmt.setString(5, record.getSymptoms());
            stmt.setString(6, record.getDiagnosis());
            stmt.setString(7, record.getTreatment());
            stmt.setString(8, record.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        record.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating medical record: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateRecord(MedicalRecord record) {
        String sql = "UPDATE medical_records SET pet_id = ?, vet_id = ?, visit_date = ?, symptoms = ?, diagnosis = ?, treatment = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, record.getPetId());
            stmt.setInt(2, record.getVetId());
            stmt.setDate(3, record.getVisitDate());
            stmt.setString(4, record.getSymptoms());
            stmt.setString(5, record.getDiagnosis());
            stmt.setString(6, record.getTreatment());
            stmt.setString(7, record.getNotes());
            stmt.setInt(8, record.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medical record #{}: {}", record.getId(), e.getMessage());
        }
        return false;
    }

    public boolean deleteRecord(int id) {
        String sql = "DELETE FROM medical_records WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting medical record #{}: {}", id, e.getMessage());
        }
        return false;
    }

    public List<Prescription> getPrescriptionsForRecord(Connection conn, int recordId) throws SQLException {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT pr.*, m.name AS medicine_name " +
                     "FROM prescriptions pr " +
                     "JOIN medicines m ON pr.medicine_id = m.id " +
                     "WHERE pr.medical_record_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prescription p = new Prescription();
                    p.setId(rs.getInt("id"));
                    p.setMedicalRecordId(rs.getInt("medical_record_id"));
                    p.setMedicineId(rs.getInt("medicine_id"));
                    p.setMedicineName(rs.getString("medicine_name"));
                    p.setDosage(rs.getString("dosage"));
                    p.setFrequency(rs.getString("frequency"));
                    p.setDurationDays(rs.getInt("duration_days"));
                    p.setInstructions(rs.getString("instructions"));
                    p.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean addPrescription(Prescription p) {
        String sql = "INSERT INTO prescriptions (medical_record_id, medicine_id, dosage, frequency, duration_days, instructions) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, p.getMedicalRecordId());
            stmt.setInt(2, p.getMedicineId());
            stmt.setString(3, p.getDosage());
            stmt.setString(4, p.getFrequency());
            stmt.setInt(5, p.getDurationDays());
            stmt.setString(6, p.getInstructions());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding prescription: {}", e.getMessage());
        }
        return false;
    }

    private MedicalRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        MedicalRecord r = new MedicalRecord();
        r.setId(rs.getInt("id"));
        r.setPetId(rs.getInt("pet_id"));
        r.setVetId(rs.getInt("vet_id"));
        int apptId = rs.getInt("appointment_id");
        r.setAppointmentId(rs.wasNull() ? null : apptId);
        r.setPetName(rs.getString("pet_name"));
        r.setVetName(rs.getString("vet_name"));
        r.setVisitDate(rs.getDate("visit_date"));
        r.setSymptoms(rs.getString("symptoms"));
        r.setDiagnosis(rs.getString("diagnosis"));
        r.setTreatment(rs.getString("treatment"));
        r.setNotes(rs.getString("notes"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}

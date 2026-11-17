package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Invoice;
import com.petcare.model.Payment;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceDAO.class);

    public List<Invoice> getAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT i.*, p.name AS pet_name, CONCAT(o.first_name, ' ', o.last_name) AS owner_name " +
                     "FROM invoices i " +
                     "JOIN pets p ON i.pet_id = p.id " +
                     "JOIN pet_owners o ON i.owner_id = o.id " +
                     "ORDER BY i.invoice_date DESC, i.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching invoices: {}", e.getMessage());
        }
        return list;
    }

    public String generateNextInvoiceNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "INV-" + year + "-";
        String sql = "SELECT MAX(CAST(SUBSTRING(invoice_number, 10) AS UNSIGNED)) FROM invoices WHERE invoice_number LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int nextSeq = rs.getInt(1) + 1;
                    return String.format("%s%04d", prefix, nextSeq);
                }
            }
        } catch (SQLException e) {
            logger.error("Error generating invoice number: {}", e.getMessage());
        }
        return prefix + "0001";
    }

    public boolean createInvoice(Invoice inv) {
        String sql = "INSERT INTO invoices (invoice_number, appointment_id, pet_id, owner_id, invoice_date, consultation_fee, treatment_fee, medicine_total, tax_amount, discount_amount, total_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (inv.getInvoiceNumber() == null || inv.getInvoiceNumber().isEmpty()) {
                inv.setInvoiceNumber(generateNextInvoiceNumber());
            }

            stmt.setString(1, inv.getInvoiceNumber());
            if (inv.getAppointmentId() != null) {
                stmt.setInt(2, inv.getAppointmentId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, inv.getPetId());
            stmt.setInt(4, inv.getOwnerId());
            stmt.setDate(5, inv.getInvoiceDate());
            stmt.setBigDecimal(6, inv.getConsultationFee());
            stmt.setBigDecimal(7, inv.getTreatmentFee());
            stmt.setBigDecimal(8, inv.getMedicineTotal());
            stmt.setBigDecimal(9, inv.getTaxAmount());
            stmt.setBigDecimal(10, inv.getDiscountAmount());
            stmt.setBigDecimal(11, inv.getTotalAmount());
            stmt.setString(12, inv.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        inv.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating invoice: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int invoiceId, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating invoice status #{}: {}", invoiceId, e.getMessage());
        }
        return false;
    }

    public boolean recordPayment(Payment p) {
        String sql = "INSERT INTO payments (invoice_id, payment_date, amount, payment_method, transaction_reference, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, p.getInvoiceId());
            stmt.setDate(2, p.getPaymentDate());
            stmt.setBigDecimal(3, p.getAmount());
            stmt.setString(4, p.getPaymentMethod());
            stmt.setString(5, p.getTransactionReference());
            stmt.setString(6, p.getNotes());

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                updateStatus(p.getInvoiceId(), "PAID");
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error recording payment: {}", e.getMessage());
        }
        return false;
    }

    public List<Payment> getPaymentsForInvoice(int invoiceId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE invoice_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setInvoiceId(rs.getInt("invoice_id"));
                    p.setPaymentDate(rs.getDate("payment_date"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setTransactionReference(rs.getString("transaction_reference"));
                    p.setNotes(rs.getString("notes"));
                    p.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching payments for invoice #{}: {}", invoiceId, e.getMessage());
        }
        return list;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setId(rs.getInt("id"));
        i.setInvoiceNumber(rs.getString("invoice_number"));
        int apptId = rs.getInt("appointment_id");
        i.setAppointmentId(rs.wasNull() ? null : apptId);
        i.setPetId(rs.getInt("pet_id"));
        i.setOwnerId(rs.getInt("owner_id"));
        i.setPetName(rs.getString("pet_name"));
        i.setOwnerName(rs.getString("owner_name"));
        i.setInvoiceDate(rs.getDate("invoice_date"));
        i.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        i.setTreatmentFee(rs.getBigDecimal("treatment_fee"));
        i.setMedicineTotal(rs.getBigDecimal("medicine_total"));
        i.setTaxAmount(rs.getBigDecimal("tax_amount"));
        i.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        i.setTotalAmount(rs.getBigDecimal("total_amount"));
        i.setStatus(rs.getString("status"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        return i;
    }
}

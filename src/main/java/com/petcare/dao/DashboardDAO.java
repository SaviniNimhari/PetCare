package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.DashboardStats;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardDAO {
    private static final Logger logger = LoggerFactory.getLogger(DashboardDAO.class);

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            stats.setTotalPets(getSingleCount(conn, "SELECT COUNT(*) FROM pets"));
            stats.setTotalOwners(getSingleCount(conn, "SELECT COUNT(*) FROM pet_owners"));
            stats.setTotalVeterinarians(getSingleCount(conn, "SELECT COUNT(*) FROM veterinarians"));
            stats.setTodayAppointments(getSingleCount(conn, "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()"));
            stats.setPendingAppointments(getSingleCount(conn, "SELECT COUNT(*) FROM appointments WHERE status = 'SCHEDULED'"));
            stats.setLowStockMedicines(getSingleCount(conn, "SELECT COUNT(*) FROM medicines WHERE quantity_in_stock <= min_stock_level"));
            stats.setUpcomingVaccinations(getSingleCount(conn, "SELECT COUNT(*) FROM vaccinations WHERE next_due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)"));
            
            // Monthly revenue from payments
            String revSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE MONTH(payment_date) = MONTH(CURDATE()) AND YEAR(payment_date) = YEAR(CURDATE())";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(revSql)) {
                if (rs.next()) {
                    stats.setMonthlyRevenue(rs.getBigDecimal(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error gathering dashboard stats: {}", e.getMessage());
        }
        return stats;
    }

    public Map<String, Integer> getAppointmentsByStatus() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT status, COUNT(*) FROM appointments GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            logger.error("Error getting appointments by status: {}", e.getMessage());
        }
        return map;
    }

    public Map<String, BigDecimal> getRevenueLast6Months() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(payment_date, '%b %Y') AS m_label, COALESCE(SUM(amount), 0) AS total " +
                     "FROM payments " +
                     "WHERE payment_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                     "GROUP BY DATE_FORMAT(payment_date, '%Y-%m'), DATE_FORMAT(payment_date, '%b %Y') " +
                     "ORDER BY DATE_FORMAT(payment_date, '%Y-%m') ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("m_label"), rs.getBigDecimal("total"));
            }
        } catch (SQLException e) {
            logger.error("Error getting monthly revenue: {}", e.getMessage());
        }
        return map;
    }

    private int getSingleCount(Connection conn, String sql) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error executing count query [{}]: {}", sql, e.getMessage());
        }
        return 0;
    }
}

package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Medicine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedicineDAO {
    private static final Logger logger = LoggerFactory.getLogger(MedicineDAO.class);

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching medicines: {}", e.getMessage());
        }
        return list;
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE quantity_in_stock <= min_stock_level ORDER BY quantity_in_stock ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToMedicine(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching low-stock medicines: {}", e.getMessage());
        }
        return list;
    }

    public List<Medicine> searchMedicines(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM medicines WHERE name LIKE ? OR category LIKE ? OR manufacturer LIKE ? ORDER BY name ASC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicine(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching medicines: {}", e.getMessage());
        }
        return list;
    }

    public boolean createMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, category, dosage_form, strength, quantity_in_stock, min_stock_level, unit_price, expiry_date, manufacturer) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, m.getName());
            stmt.setString(2, m.getCategory());
            stmt.setString(3, m.getDosageForm());
            stmt.setString(4, m.getStrength());
            stmt.setInt(5, m.getQuantityInStock());
            stmt.setInt(6, m.getMinStockLevel());
            stmt.setBigDecimal(7, m.getUnitPrice());
            stmt.setDate(8, m.getExpiryDate());
            stmt.setString(9, m.getManufacturer());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        m.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating medicine: {}", e.getMessage());
        }
        return false;
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name = ?, category = ?, dosage_form = ?, strength = ?, quantity_in_stock = ?, min_stock_level = ?, unit_price = ?, expiry_date = ?, manufacturer = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, m.getName());
            stmt.setString(2, m.getCategory());
            stmt.setString(3, m.getDosageForm());
            stmt.setString(4, m.getStrength());
            stmt.setInt(5, m.getQuantityInStock());
            stmt.setInt(6, m.getMinStockLevel());
            stmt.setBigDecimal(7, m.getUnitPrice());
            stmt.setDate(8, m.getExpiryDate());
            stmt.setString(9, m.getManufacturer());
            stmt.setInt(10, m.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medicine #{}: {}", m.getId(), e.getMessage());
        }
        return false;
    }

    public boolean updateStock(int medicineId, int quantityChange) {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, medicineId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adjusting stock for medicine #{}: {}", medicineId, e.getMessage());
        }
        return false;
    }

    public boolean deleteMedicine(int id) {
        String sql = "DELETE FROM medicines WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting medicine #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setDosageForm(rs.getString("dosage_form"));
        m.setStrength(rs.getString("strength"));
        m.setQuantityInStock(rs.getInt("quantity_in_stock"));
        m.setMinStockLevel(rs.getInt("min_stock_level"));
        m.setUnitPrice(rs.getBigDecimal("unit_price"));
        m.setExpiryDate(rs.getDate("expiry_date"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }
}

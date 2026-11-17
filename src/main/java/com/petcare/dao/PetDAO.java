package com.petcare.dao;

import com.petcare.config.DatabaseConnection;
import com.petcare.model.Pet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetDAO {
    private static final Logger logger = LoggerFactory.getLogger(PetDAO.class);

    public List<Pet> getAllPets() {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(o.first_name, ' ', o.last_name) AS owner_name " +
                     "FROM pets p " +
                     "JOIN pet_owners o ON p.owner_id = o.id " +
                     "ORDER BY p.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                pets.add(mapResultSetToPet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching pets: {}", e.getMessage());
        }
        return pets;
    }

    public List<Pet> getPetsByOwnerId(int ownerId) {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(o.first_name, ' ', o.last_name) AS owner_name " +
                     "FROM pets p " +
                     "JOIN pet_owners o ON p.owner_id = o.id " +
                     "WHERE p.owner_id = ? " +
                     "ORDER BY p.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching pets for owner #{}: {}", ownerId, e.getMessage());
        }
        return pets;
    }

    public List<Pet> searchPets(String keyword) {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT p.*, CONCAT(o.first_name, ' ', o.last_name) AS owner_name " +
                     "FROM pets p " +
                     "JOIN pet_owners o ON p.owner_id = o.id " +
                     "WHERE p.name LIKE ? OR p.species LIKE ? OR p.breed LIKE ? OR p.microchip_id LIKE ? OR CONCAT(o.first_name, ' ', o.last_name) LIKE ? " +
                     "ORDER BY p.name ASC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            stmt.setString(5, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pets.add(mapResultSetToPet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching pets: {}", e.getMessage());
        }
        return pets;
    }

    public Pet getPetById(int id) {
        String sql = "SELECT p.*, CONCAT(o.first_name, ' ', o.last_name) AS owner_name " +
                     "FROM pets p " +
                     "JOIN pet_owners o ON p.owner_id = o.id " +
                     "WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching pet #{}: {}", id, e.getMessage());
        }
        return null;
    }

    public boolean createPet(Pet pet) {
        String sql = "INSERT INTO pets (owner_id, name, species, breed, gender, date_of_birth, weight_kg, color, microchip_id, photo_url, allergies_or_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, pet.getOwnerId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getSpecies());
            stmt.setString(4, pet.getBreed());
            stmt.setString(5, pet.getGender());
            stmt.setDate(6, pet.getDateOfBirth());
            stmt.setBigDecimal(7, pet.getWeightKg());
            stmt.setString(8, pet.getColor());
            stmt.setString(9, pet.getMicrochipId());
            stmt.setString(10, pet.getPhotoUrl());
            stmt.setString(11, pet.getAllergiesOrNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        pet.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating pet: {}", e.getMessage());
        }
        return false;
    }

    public boolean updatePet(Pet pet) {
        String sql = "UPDATE pets SET owner_id = ?, name = ?, species = ?, breed = ?, gender = ?, date_of_birth = ?, weight_kg = ?, color = ?, microchip_id = ?, photo_url = ?, allergies_or_notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pet.getOwnerId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getSpecies());
            stmt.setString(4, pet.getBreed());
            stmt.setString(5, pet.getGender());
            stmt.setDate(6, pet.getDateOfBirth());
            stmt.setBigDecimal(7, pet.getWeightKg());
            stmt.setString(8, pet.getColor());
            stmt.setString(9, pet.getMicrochipId());
            stmt.setString(10, pet.getPhotoUrl());
            stmt.setString(11, pet.getAllergiesOrNotes());
            stmt.setInt(12, pet.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating pet #{}: {}", pet.getId(), e.getMessage());
        }
        return false;
    }

    public boolean deletePet(int id) {
        String sql = "DELETE FROM pets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting pet #{}: {}", id, e.getMessage());
        }
        return false;
    }

    private Pet mapResultSetToPet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getInt("id"));
        pet.setOwnerId(rs.getInt("owner_id"));
        try {
            pet.setOwnerName(rs.getString("owner_name"));
        } catch (SQLException ignored) {}
        pet.setName(rs.getString("name"));
        pet.setSpecies(rs.getString("species"));
        pet.setBreed(rs.getString("breed"));
        pet.setGender(rs.getString("gender"));
        pet.setDateOfBirth(rs.getDate("date_of_birth"));
        pet.setWeightKg(rs.getBigDecimal("weight_kg"));
        pet.setColor(rs.getString("color"));
        pet.setMicrochipId(rs.getString("microchip_id"));
        pet.setPhotoUrl(rs.getString("photo_url"));
        pet.setAllergiesOrNotes(rs.getString("allergies_or_notes"));
        pet.setCreatedAt(rs.getTimestamp("created_at"));
        return pet;
    }
}

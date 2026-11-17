package com.petcare.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Pet {
    private int id;
    private int ownerId;
    private String ownerName; // joined property for UI table
    private String name;
    private String species;
    private String breed;
    private String gender; // MALE, FEMALE
    private Date dateOfBirth;
    private BigDecimal weightKg;
    private String color;
    private String microchipId;
    private String photoUrl;
    private String allergiesOrNotes;
    private Timestamp createdAt;

    public Pet() {
        this.gender = "MALE";
    }

    public Pet(int id, int ownerId, String name, String species, String breed, String gender, Date dateOfBirth, BigDecimal weightKg) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.weightKg = weightKg;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMicrochipId() { return microchipId; }
    public void setMicrochipId(String microchipId) { this.microchipId = microchipId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getAllergiesOrNotes() { return allergiesOrNotes; }
    public void setAllergiesOrNotes(String allergiesOrNotes) { this.allergiesOrNotes = allergiesOrNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + species + (breed != null ? " - " + breed : "") + ")";
    }
}

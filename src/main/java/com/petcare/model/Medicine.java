package com.petcare.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Medicine {
    private int id;
    private String name;
    private String category;
    private String dosageForm; // Tablet, Syrup, Injection, Ointment, etc.
    private String strength;
    private int quantityInStock;
    private int minStockLevel;
    private BigDecimal unitPrice;
    private Date expiryDate;
    private String manufacturer;
    private Timestamp createdAt;

    public Medicine() {
        this.minStockLevel = 10;
        this.unitPrice = BigDecimal.ZERO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isLowStock() {
        return quantityInStock <= minStockLevel;
    }

    @Override
    public String toString() {
        return name + " (" + strength + ", " + dosageForm + ")";
    }
}

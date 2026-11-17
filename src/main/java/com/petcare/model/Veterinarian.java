package com.petcare.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Veterinarian {
    private int id;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String phone;
    private String email;
    private String availableDays;
    private BigDecimal consultationFee;
    private Timestamp createdAt;

    public Veterinarian() {
        this.consultationFee = new BigDecimal("50.00");
    }

    public Veterinarian(int id, String firstName, String lastName, String specialization, String licenseNumber, String phone, String email, BigDecimal consultationFee) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.email = email;
        this.consultationFee = consultationFee;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        return "Dr. " + (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvailableDays() { return availableDays; }
    public void setAvailableDays(String availableDays) { this.availableDays = availableDays; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getFullName() + " - " + (specialization != null ? specialization : "General");
    }
}

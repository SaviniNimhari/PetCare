package com.petcare.model;

import java.sql.Date;
import java.sql.Timestamp;

public class Vaccination {
    private int id;
    private int petId;
    private String petName;
    private String vaccineName;
    private Date administeredDate;
    private Date nextDueDate;
    private String batchNumber;
    private Integer administeredByVetId;
    private String vetName;
    private String status; // GIVEN, UPCOMING, OVERDUE
    private String notes;
    private Timestamp createdAt;

    public Vaccination() {
        this.status = "GIVEN";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public Date getAdministeredDate() { return administeredDate; }
    public void setAdministeredDate(Date administeredDate) { this.administeredDate = administeredDate; }

    public Date getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(Date nextDueDate) { this.nextDueDate = nextDueDate; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Integer getAdministeredByVetId() { return administeredByVetId; }
    public void setAdministeredByVetId(Integer administeredByVetId) { this.administeredByVetId = administeredByVetId; }

    public String getVetName() { return vetName; }
    public void setVetName(String vetName) { this.vetName = vetName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

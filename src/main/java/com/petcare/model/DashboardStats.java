package com.petcare.model;

import java.math.BigDecimal;

public class DashboardStats {
    private int totalPets;
    private int totalOwners;
    private int totalVeterinarians;
    private int todayAppointments;
    private int pendingAppointments;
    private int lowStockMedicines;
    private int upcomingVaccinations;
    private BigDecimal monthlyRevenue;

    public DashboardStats() {
        this.monthlyRevenue = BigDecimal.ZERO;
    }

    public int getTotalPets() { return totalPets; }
    public void setTotalPets(int totalPets) { this.totalPets = totalPets; }

    public int getTotalOwners() { return totalOwners; }
    public void setTotalOwners(int totalOwners) { this.totalOwners = totalOwners; }

    public int getTotalVeterinarians() { return totalVeterinarians; }
    public void setTotalVeterinarians(int totalVeterinarians) { this.totalVeterinarians = totalVeterinarians; }

    public int getTodayAppointments() { return todayAppointments; }
    public void setTodayAppointments(int todayAppointments) { this.todayAppointments = todayAppointments; }

    public int getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(int pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public int getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(int lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }

    public int getUpcomingVaccinations() { return upcomingVaccinations; }
    public void setUpcomingVaccinations(int upcomingVaccinations) { this.upcomingVaccinations = upcomingVaccinations; }

    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
}

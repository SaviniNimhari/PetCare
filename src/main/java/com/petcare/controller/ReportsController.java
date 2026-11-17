package com.petcare.controller;

import com.petcare.dao.AppointmentDAO;
import com.petcare.dao.DashboardDAO;
import com.petcare.dao.InvoiceDAO;
import com.petcare.dao.MedicineDAO;
import com.petcare.dao.PetDAO;
import com.petcare.model.Appointment;
import com.petcare.model.Invoice;
import com.petcare.model.Medicine;
import com.petcare.model.Pet;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ReportsController {

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Label reportHeaderLabel;
    @FXML private TextArea reportOutputArea;

    private final PetDAO petDAO = new PetDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        reportTypeCombo.getItems().addAll(
            "Executive Clinic Summary",
            "Revenue & Billing Audit",
            "Appointment Activity & Status",
            "Pharmacy & Inventory Expiry Audit",
            "Patient Demographics by Species"
        );
        reportTypeCombo.setValue("Executive Clinic Summary");
        reportTypeCombo.setOnAction(e -> generateReport());

        generateReport();
    }

    @FXML
    public void generateReport() {
        String selected = reportTypeCombo.getValue();
        if (selected == null) return;

        reportHeaderLabel.setText("Report: " + selected + " (Generated on " + LocalDate.now() + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("            🐾 PETCARE VETERINARY CLINIC MANAGEMENT SYSTEM\n");
        sb.append("                  EXECUTIVE AUDIT & ANALYTICS REPORT\n");
        sb.append("========================================================================\n\n");
        sb.append("Report Type:  ").append(selected).append("\n");
        sb.append("Generated At: ").append(LocalDate.now()).append("\n");
        sb.append("------------------------------------------------------------------------\n\n");

        switch (selected) {
            case "Executive Clinic Summary":
                appendExecutiveSummary(sb);
                break;
            case "Revenue & Billing Audit":
                appendRevenueAudit(sb);
                break;
            case "Appointment Activity & Status":
                appendAppointmentActivity(sb);
                break;
            case "Pharmacy & Inventory Expiry Audit":
                appendInventoryAudit(sb);
                break;
            case "Patient Demographics by Species":
                appendPatientDemographics(sb);
                break;
        }

        sb.append("\n========================================================================\n");
        sb.append("                   End of Official PetCare Report\n");
        sb.append("========================================================================\n");

        reportOutputArea.setText(sb.toString());
    }

    private void appendExecutiveSummary(StringBuilder sb) {
        var stats = dashboardDAO.getDashboardStats();
        sb.append("1. CLINIC CAPACITY & VOLUME:\n");
        sb.append("   - Total Registered Pets:       ").append(stats.getTotalPets()).append("\n");
        sb.append("   - Total Pet Owners:            ").append(stats.getTotalOwners()).append("\n");
        sb.append("   - Active Veterinarians:        ").append(stats.getTotalVeterinarians()).append("\n");
        sb.append("   - Today's Appointments:        ").append(stats.getTodayAppointments()).append("\n\n");
        sb.append("2. FINANCIAL HIGHLIGHTS:\n");
        sb.append("   - Current Month Revenue:       ").append(currencyFormat.format(stats.getMonthlyRevenue())).append("\n\n");
        sb.append("3. OPERATIONAL RISK ALERTS:\n");
        sb.append("   - Medicines Low in Stock:      ").append(stats.getLowStockMedicines()).append(" item(s)\n");
        sb.append("   - Vaccinations Due Soon:       ").append(stats.getUpcomingVaccinations()).append(" patient(s)\n");
    }

    private void appendRevenueAudit(StringBuilder sb) {
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        BigDecimal totalBilled = invoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidCount = invoices.stream().filter(i -> "PAID".equalsIgnoreCase(i.getStatus())).count();
        long pendingCount = invoices.stream().filter(i -> "PENDING".equalsIgnoreCase(i.getStatus())).count();

        sb.append("FINANCIAL AUDIT SUMMARY:\n");
        sb.append("   - Total Invoices Generated:    ").append(invoices.size()).append("\n");
        sb.append("   - Fully Settled Invoices:      ").append(paidCount).append("\n");
        sb.append("   - Pending / Unpaid Invoices:   ").append(pendingCount).append("\n");
        sb.append("   - Cumulative Value Invoiced:   ").append(currencyFormat.format(totalBilled)).append("\n\n");

        sb.append(String.format("%-16s %-12s %-15s %-18s %-12s %-10s\n", "INVOICE #", "DATE", "PATIENT", "OWNER", "AMOUNT", "STATUS"));
        sb.append("------------------------------------------------------------------------\n");
        for (Invoice inv : invoices) {
            sb.append(String.format("%-16s %-12s %-15s %-18s %-12s %-10s\n",
                inv.getInvoiceNumber(),
                inv.getInvoiceDate(),
                inv.getPetName(),
                inv.getOwnerName(),
                currencyFormat.format(inv.getTotalAmount()),
                inv.getStatus()
            ));
        }
    }

    private void appendAppointmentActivity(StringBuilder sb) {
        List<Appointment> list = appointmentDAO.getAllAppointments();
        Map<String, Long> statusCounts = list.stream().collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));

        sb.append("APPOINTMENT UTILIZATION SUMMARY:\n");
        sb.append("   - Total Booked Appointments:   ").append(list.size()).append("\n");
        statusCounts.forEach((status, count) -> sb.append(String.format("   - %-25s: %d\n", status, count)));
        sb.append("\n");

        sb.append(String.format("%-6s %-12s %-9s %-14s %-18s %-12s\n", "ID", "DATE", "TIME", "PATIENT", "DOCTOR", "STATUS"));
        sb.append("------------------------------------------------------------------------\n");
        for (Appointment a : list) {
            sb.append(String.format("%-6d %-12s %-9s %-14s %-18s %-12s\n",
                a.getId(),
                a.getAppointmentDate(),
                a.getAppointmentTime(),
                a.getPetName(),
                a.getVetName(),
                a.getStatus()
            ));
        }
    }

    private void appendInventoryAudit(StringBuilder sb) {
        List<Medicine> meds = medicineDAO.getAllMedicines();
        List<Medicine> lowStock = medicineDAO.getLowStockMedicines();

        sb.append("PHARMACEUTICAL INVENTORY AUDIT:\n");
        sb.append("   - Total Tracked Medicines:     ").append(meds.size()).append("\n");
        sb.append("   - Medicines at Low Stock:      ").append(lowStock.size()).append("\n\n");

        sb.append(String.format("%-22s %-15s %-8s %-8s %-12s %-12s\n", "MEDICINE NAME", "CATEGORY", "STOCK", "MIN", "PRICE", "EXPIRY"));
        sb.append("------------------------------------------------------------------------\n");
        for (Medicine m : meds) {
            sb.append(String.format("%-22s %-15s %-8d %-8d %-12s %-12s\n",
                m.getName(),
                m.getCategory(),
                m.getQuantityInStock(),
                m.getMinStockLevel(),
                currencyFormat.format(m.getUnitPrice()),
                m.getExpiryDate()
            ));
        }
    }

    private void appendPatientDemographics(StringBuilder sb) {
        List<Pet> pets = petDAO.getAllPets();
        Map<String, Long> speciesCounts = pets.stream().collect(Collectors.groupingBy(Pet::getSpecies, Collectors.counting()));

        sb.append("PATIENT POPULATION DEMOGRAPHICS:\n");
        sb.append("   - Total Registered Patients:   ").append(pets.size()).append("\n\n");
        sb.append("SPECIES DISTRIBUTION:\n");
        speciesCounts.forEach((species, count) -> {
            double pct = pets.isEmpty() ? 0 : (count * 100.0 / pets.size());
            sb.append(String.format("   - %-15s: %3d  (%.1f%%)\n", species, count, pct));
        });
    }
}

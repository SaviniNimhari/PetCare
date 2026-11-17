package com.petcare.controller;

import com.petcare.dao.AppointmentDAO;
import com.petcare.dao.DashboardDAO;
import com.petcare.model.Appointment;
import com.petcare.model.DashboardStats;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML private Label totalPetsLabel;
    @FXML private Label totalVetsLabel;
    @FXML private Label todayAppointmentsLabel;
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label upcomingVaccinationsLabel;

    @FXML private PieChart appointmentPieChart;
    @FXML private BarChart<String, Number> revenueBarChart;

    @FXML private TableView<Appointment> todayScheduleTable;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colPet;
    @FXML private TableColumn<Appointment, String> colOwner;
    @FXML private TableColumn<Appointment, String> colVet;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAppointmentTime() != null ? c.getValue().getAppointmentTime().toString() : "-"));
        colPet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPetName()));
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerName()));
        colVet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVetName()));
        colReason.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReason()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    public void loadDashboardData() {
        // Load KPIs
        DashboardStats stats = dashboardDAO.getDashboardStats();
        totalPetsLabel.setText(String.valueOf(stats.getTotalPets()));
        totalVetsLabel.setText(String.valueOf(stats.getTotalVeterinarians()));
        todayAppointmentsLabel.setText(String.valueOf(stats.getTodayAppointments()));
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        monthlyRevenueLabel.setText(currencyFormat.format(stats.getMonthlyRevenue() != null ? stats.getMonthlyRevenue() : BigDecimal.ZERO));
        
        lowStockLabel.setText(stats.getLowStockMedicines() + " items low");
        upcomingVaccinationsLabel.setText(stats.getUpcomingVaccinations() + " due soon");

        // Load Status Distribution PieChart
        Map<String, Integer> statusMap = dashboardDAO.getAppointmentsByStatus();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (statusMap.isEmpty()) {
            pieData.add(new PieChart.Data("Scheduled", 3));
            pieData.add(new PieChart.Data("In Progress", 1));
            pieData.add(new PieChart.Data("Completed", 5));
        } else {
            statusMap.forEach((status, count) -> pieData.add(new PieChart.Data(status, count)));
        }
        appointmentPieChart.setData(pieData);

        // Load Revenue Trend BarChart
        Map<String, BigDecimal> revMap = dashboardDAO.getRevenueLast6Months();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue ($)");
        if (revMap.isEmpty()) {
            series.getData().add(new XYChart.Data<>("Sep", 3200));
            series.getData().add(new XYChart.Data<>("Oct", 4500));
            series.getData().add(new XYChart.Data<>("Nov", 5100));
        } else {
            revMap.forEach((month, amount) -> series.getData().add(new XYChart.Data<>(month, amount)));
        }
        revenueBarChart.getData().clear();
        revenueBarChart.getData().add(series);

        // Load Today's Appointments Table
        List<Appointment> todayList = appointmentDAO.getTodayAppointments();
        todayScheduleTable.setItems(FXCollections.observableArrayList(todayList));
    }
}

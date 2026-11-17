package com.petcare.controller;

import com.petcare.dao.AppointmentDAO;
import com.petcare.dao.PetDAO;
import com.petcare.dao.PetOwnerDAO;
import com.petcare.dao.VeterinarianDAO;
import com.petcare.model.Appointment;
import com.petcare.model.Pet;
import com.petcare.model.Veterinarian;
import com.petcare.util.AlertUtil;
import com.petcare.util.DateUtil;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class AppointmentController {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colId;
    @FXML private TableColumn<Appointment, String> colDate;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colPet;
    @FXML private TableColumn<Appointment, String> colOwner;
    @FXML private TableColumn<Appointment, String> colVet;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private DatePicker dateFilterPicker;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PetDAO petDAO = new PetDAO();
    private final VeterinarianDAO vetDAO = new VeterinarianDAO();
    private List<Appointment> allAppointments;

    @FXML
    public void initialize() {
        setupTable();

        statusFilterCombo.getItems().addAll("ALL", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        statusFilterCombo.setValue("ALL");
        statusFilterCombo.setOnAction(e -> applyFilters());

        dateFilterPicker.setOnAction(e -> applyFilters());

        loadAppointments();
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAppointmentDate() != null ? c.getValue().getAppointmentDate().toString() : "-"));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAppointmentTime() != null ? c.getValue().getAppointmentTime().toString() : "-"));
        colPet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPetName()));
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerName()));
        colVet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVetName()));
        colReason.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReason()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    public void loadAppointments() {
        allAppointments = appointmentDAO.getAllAppointments();
        applyFilters();
    }

    private void applyFilters() {
        if (allAppointments == null) return;

        String selectedStatus = statusFilterCombo.getValue();
        LocalDate selectedDate = dateFilterPicker.getValue();

        List<Appointment> filtered = allAppointments.stream().filter(a -> {
            boolean statusMatch = "ALL".equalsIgnoreCase(selectedStatus) || a.getStatus().equalsIgnoreCase(selectedStatus);
            boolean dateMatch = (selectedDate == null) || (a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().isEqual(selectedDate));
            return statusMatch && dateMatch;
        }).collect(Collectors.toList());

        appointmentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleResetFilters() {
        statusFilterCombo.setValue("ALL");
        dateFilterPicker.setValue(null);
        applyFilters();
    }

    @FXML
    public void handleBookAppointment() {
        showAppointmentDialog(null);
    }

    @FXML
    public void handleEditAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an appointment from the table to edit.");
            return;
        }
        showAppointmentDialog(selected);
    }

    @FXML
    public void handleCancelAppointment() {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an appointment to cancel.");
            return;
        }
        if (AlertUtil.showConfirmation("Cancel Appointment", "Are you sure you want to cancel this appointment?")) {
            appointmentDAO.updateStatus(selected.getId(), "CANCELLED");
            loadAppointments();
        }
    }

    @FXML
    public void handleMarkInProgress() {
        updateSelectedStatus("IN_PROGRESS");
    }

    @FXML
    public void handleMarkCompleted() {
        updateSelectedStatus("COMPLETED");
    }

    private void updateSelectedStatus(String newStatus) {
        Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an appointment from the table.");
            return;
        }
        if (appointmentDAO.updateStatus(selected.getId(), newStatus)) {
            AlertUtil.showInfo("Status Updated", "Appointment status updated to " + newStatus);
            loadAppointments();
        } else {
            AlertUtil.showError("Error", "Could not update appointment status.");
        }
    }

    private void showAppointmentDialog(Appointment existingAppt) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingAppt == null ? "Book New Appointment" : "Reschedule / Edit Appointment");
        dialog.setHeaderText("Select pet, veterinarian and scheduling slot:");

        ButtonType saveBtnType = new ButtonType("Confirm Booking", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Pet> petCombo = new ComboBox<>();
        petCombo.getItems().addAll(petDAO.getAllPets());
        petCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Veterinarian> vetCombo = new ComboBox<>();
        vetCombo.getItems().addAll(vetDAO.getAllVeterinarians());
        vetCombo.setMaxWidth(Double.MAX_VALUE);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll(
            "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
            "13:30:00", "14:00:00", "14:30:00", "15:00:00", "15:30:00", "16:00:00", "16:30:00"
        );
        timeCombo.setValue("10:00:00");

        TextField reasonField = new TextField();
        reasonField.setPromptText("e.g. Vaccination booster, limping, regular checkup");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Additional vet or owner notes...");
        notesArea.setPrefRowCount(2);

        if (existingAppt != null) {
            for (Pet p : petCombo.getItems()) {
                if (p.getId() == existingAppt.getPetId()) {
                    petCombo.setValue(p);
                    break;
                }
            }
            for (Veterinarian v : vetCombo.getItems()) {
                if (v.getId() == existingAppt.getVetId()) {
                    vetCombo.setValue(v);
                    break;
                }
            }
            if (existingAppt.getAppointmentDate() != null) {
                datePicker.setValue(existingAppt.getAppointmentDate().toLocalDate());
            }
            if (existingAppt.getAppointmentTime() != null) {
                timeCombo.setValue(existingAppt.getAppointmentTime().toString());
            }
            reasonField.setText(existingAppt.getReason());
            notesArea.setText(existingAppt.getNotes());
        }

        grid.add(new Label("Select Pet: *"), 0, 0);
        grid.add(petCombo, 1, 0);
        grid.add(new Label("Select Veterinarian: *"), 0, 1);
        grid.add(vetCombo, 1, 1);
        grid.add(new Label("Appointment Date: *"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Appointment Time: *"), 0, 3);
        grid.add(timeCombo, 1, 3);
        grid.add(new Label("Reason / Purpose: *"), 0, 4);
        grid.add(reasonField, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (petCombo.getValue() == null || vetCombo.getValue() == null || datePicker.getValue() == null ||
                    timeCombo.getValue() == null || reasonField.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please fill in all mandatory booking fields.");
                    return;
                }

                Date apptDate = Date.valueOf(datePicker.getValue());
                Time apptTime = Time.valueOf(timeCombo.getValue());
                int vetId = vetCombo.getValue().getId();
                int excludeId = existingAppt != null ? existingAppt.getId() : 0;

                // Professional feature: Appointment conflict detection
                if (appointmentDAO.hasConflict(vetId, apptDate, apptTime, excludeId)) {
                    AlertUtil.showWarning("Scheduling Conflict Detected",
                        "Dr. " + vetCombo.getValue().getLastName() + " already has an appointment booked at " +
                        apptTime + " on " + apptDate + ".\nPlease pick another time or veterinarian.");
                    return;
                }

                Appointment appt = existingAppt != null ? existingAppt : new Appointment();
                appt.setPetId(petCombo.getValue().getId());
                appt.setOwnerId(petCombo.getValue().getOwnerId());
                appt.setVetId(vetId);
                appt.setAppointmentDate(apptDate);
                appt.setAppointmentTime(apptTime);
                appt.setReason(reasonField.getText().trim());
                appt.setNotes(notesArea.getText().trim());
                if (existingAppt == null) {
                    appt.setStatus("SCHEDULED");
                }

                boolean success = existingAppt == null ? appointmentDAO.createAppointment(appt) : appointmentDAO.updateAppointment(appt);
                if (success) {
                    AlertUtil.showInfo("Appointment Confirmed", "Appointment successfully saved.");
                    loadAppointments();
                } else {
                    AlertUtil.showError("Error", "Could not save appointment.");
                }
            }
        });
    }
}

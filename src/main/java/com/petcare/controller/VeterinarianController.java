package com.petcare.controller;

import com.petcare.dao.VeterinarianDAO;
import com.petcare.model.Veterinarian;
import com.petcare.util.AlertUtil;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class VeterinarianController {

    @FXML private TableView<Veterinarian> vetTable;
    @FXML private TableColumn<Veterinarian, String> colVetName;
    @FXML private TableColumn<Veterinarian, String> colSpecialization;
    @FXML private TableColumn<Veterinarian, String> colLicense;
    @FXML private TableColumn<Veterinarian, String> colFee;
    @FXML private TableColumn<Veterinarian, String> colDays;
    @FXML private TableColumn<Veterinarian, String> colPhone;
    @FXML private TableColumn<Veterinarian, String> colEmail;

    private final VeterinarianDAO vetDAO = new VeterinarianDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        setupTable();
        loadVeterinarians();
    }

    private void setupTable() {
        colVetName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colSpecialization.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecialization()));
        colLicense.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLicenseNumber()));
        colFee.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getConsultationFee() != null ? currencyFormat.format(c.getValue().getConsultationFee()) : "$0.00"));
        colDays.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAvailableDays()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
    }

    public void loadVeterinarians() {
        List<Veterinarian> list = vetDAO.getAllVeterinarians();
        vetTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleAddVet() {
        showVetDialog(null);
    }

    @FXML
    public void handleEditVet() {
        Veterinarian selected = vetTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a veterinarian to edit.");
            return;
        }
        showVetDialog(selected);
    }

    @FXML
    public void handleDeleteVet() {
        Veterinarian selected = vetTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a veterinarian to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Delete Veterinarian", "Are you sure you want to remove " + selected.getFullName() + "?")) {
            if (vetDAO.deleteVeterinarian(selected.getId())) {
                AlertUtil.showInfo("Deleted", "Veterinarian removed successfully.");
                loadVeterinarians();
            } else {
                AlertUtil.showError("Error", "Could not remove veterinarian. Ensure no existing appointments depend on this doctor.");
            }
        }
    }

    private void showVetDialog(Veterinarian existingVet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingVet == null ? "Add New Veterinarian" : "Edit Veterinarian Details");
        dialog.setHeaderText("Enter veterinarian profile and clinic schedule details:");

        ButtonType saveBtnType = new ButtonType("Save Doctor", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField firstField = new TextField();
        TextField lastField = new TextField();
        TextField specField = new TextField();
        TextField licField = new TextField();
        TextField feeField = new TextField("60.00");
        TextField daysField = new TextField("Mon, Wed, Fri");
        TextField phoneField = new TextField();
        TextField emailField = new TextField();

        if (existingVet != null) {
            firstField.setText(existingVet.getFirstName());
            lastField.setText(existingVet.getLastName());
            specField.setText(existingVet.getSpecialization());
            licField.setText(existingVet.getLicenseNumber());
            if (existingVet.getConsultationFee() != null) {
                feeField.setText(existingVet.getConsultationFee().toString());
            }
            daysField.setText(existingVet.getAvailableDays());
            phoneField.setText(existingVet.getPhone());
            emailField.setText(existingVet.getEmail());
        }

        grid.add(new Label("First Name: *"), 0, 0);
        grid.add(firstField, 1, 0);
        grid.add(new Label("Last Name: *"), 0, 1);
        grid.add(lastField, 1, 1);
        grid.add(new Label("Specialization: *"), 0, 2);
        grid.add(specField, 1, 2);
        grid.add(new Label("License Number: *"), 0, 3);
        grid.add(licField, 1, 3);
        grid.add(new Label("Consultation Fee ($): *"), 0, 4);
        grid.add(feeField, 1, 4);
        grid.add(new Label("Available Days:"), 0, 5);
        grid.add(daysField, 1, 5);
        grid.add(new Label("Phone: *"), 0, 6);
        grid.add(phoneField, 1, 6);
        grid.add(new Label("Email: *"), 0, 7);
        grid.add(emailField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (firstField.getText().trim().isEmpty() || lastField.getText().trim().isEmpty() ||
                    licField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please fill in all required fields.");
                    return;
                }

                Veterinarian vet = existingVet != null ? existingVet : new Veterinarian();
                vet.setFirstName(firstField.getText().trim());
                vet.setLastName(lastField.getText().trim());
                vet.setSpecialization(specField.getText().trim());
                vet.setLicenseNumber(licField.getText().trim());
                try {
                    vet.setConsultationFee(new BigDecimal(feeField.getText().trim()));
                } catch (Exception e) {
                    vet.setConsultationFee(new BigDecimal("50.00"));
                }
                vet.setAvailableDays(daysField.getText().trim());
                vet.setPhone(phoneField.getText().trim());
                vet.setEmail(emailField.getText().trim());

                boolean success = existingVet == null ? vetDAO.createVeterinarian(vet) : vetDAO.updateVeterinarian(vet);
                if (success) {
                    AlertUtil.showInfo("Success", "Veterinarian profile saved.");
                    loadVeterinarians();
                } else {
                    AlertUtil.showError("Error", "Could not save veterinarian profile.");
                }
            }
        });
    }
}

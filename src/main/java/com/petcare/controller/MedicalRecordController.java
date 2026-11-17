package com.petcare.controller;

import com.petcare.dao.*;
import com.petcare.model.*;
import com.petcare.util.AlertUtil;
import com.petcare.util.DateUtil;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MedicalRecordController {

    // Tab 1: Clinical Records
    @FXML private ComboBox<Pet> petSelectorCombo;
    @FXML private TableView<MedicalRecord> recordsTable;
    @FXML private TableColumn<MedicalRecord, String> colDate;
    @FXML private TableColumn<MedicalRecord, String> colPet;
    @FXML private TableColumn<MedicalRecord, String> colVet;
    @FXML private TableColumn<MedicalRecord, String> colDiagnosis;
    @FXML private TableColumn<MedicalRecord, String> colTreatment;

    @FXML private Label recordDetailsLabel;
    @FXML private ListView<String> prescriptionsListView;

    // Tab 2: Vaccinations
    @FXML private TableView<Vaccination> vaccinationTable;
    @FXML private TableColumn<Vaccination, String> colVacPet;
    @FXML private TableColumn<Vaccination, String> colVacName;
    @FXML private TableColumn<Vaccination, String> colVacAdminDate;
    @FXML private TableColumn<Vaccination, String> colVacDueDate;
    @FXML private TableColumn<Vaccination, String> colVacBatch;
    @FXML private TableColumn<Vaccination, String> colVacVet;
    @FXML private TableColumn<Vaccination, String> colVacStatus;

    private final MedicalRecordDAO recordDAO = new MedicalRecordDAO();
    private final VaccinationDAO vaccinationDAO = new VaccinationDAO();
    private final PetDAO petDAO = new PetDAO();
    private final VeterinarianDAO vetDAO = new VeterinarianDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @FXML
    public void initialize() {
        setupRecordTable();
        setupVaccinationTable();
        loadPetSelector();
        loadVaccinations();

        recordsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selected) -> {
            if (selected != null) {
                showRecordDetails(selected);
            }
        });
    }

    private void setupRecordTable() {
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVisitDate() != null ? c.getValue().getVisitDate().toString() : "-"));
        colPet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPetName()));
        colVet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVetName()));
        colDiagnosis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDiagnosis()));
        colTreatment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTreatment()));
    }

    private void setupVaccinationTable() {
        colVacPet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPetName()));
        colVacName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVaccineName()));
        colVacAdminDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAdministeredDate() != null ? c.getValue().getAdministeredDate().toString() : "-"));
        colVacDueDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNextDueDate() != null ? c.getValue().getNextDueDate().toString() : "-"));
        colVacBatch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatchNumber()));
        colVacVet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVetName()));
        colVacStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    private void loadPetSelector() {
        List<Pet> pets = petDAO.getAllPets();
        petSelectorCombo.getItems().clear();
        petSelectorCombo.getItems().addAll(pets);
        if (!pets.isEmpty()) {
            petSelectorCombo.setValue(pets.get(0));
            loadRecordsForSelectedPet();
        }
        petSelectorCombo.setOnAction(e -> loadRecordsForSelectedPet());
    }

    public void loadRecordsForSelectedPet() {
        Pet selected = petSelectorCombo.getValue();
        if (selected != null) {
            List<MedicalRecord> list = recordDAO.getRecordsByPetId(selected.getId());
            recordsTable.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                recordsTable.getSelectionModel().select(0);
            } else {
                recordDetailsLabel.setText("No clinical history found for " + selected.getName() + ".");
                prescriptionsListView.getItems().clear();
            }
        }
    }

    private void showRecordDetails(MedicalRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient: ").append(r.getPetName()).append("\n");
        sb.append("Attending: ").append(r.getVetName()).append("\n");
        sb.append("Date: ").append(r.getVisitDate()).append("\n\n");
        sb.append("Symptoms: ").append(r.getSymptoms() != null ? r.getSymptoms() : "None noted").append("\n");
        sb.append("Diagnosis: ").append(r.getDiagnosis()).append("\n");
        sb.append("Treatment: ").append(r.getTreatment()).append("\n");
        if (r.getNotes() != null && !r.getNotes().isEmpty()) {
            sb.append("Vet Notes: ").append(r.getNotes()).append("\n");
        }
        recordDetailsLabel.setText(sb.toString());

        prescriptionsListView.getItems().clear();
        if (r.getPrescriptions() != null && !r.getPrescriptions().isEmpty()) {
            for (Prescription p : r.getPrescriptions()) {
                prescriptionsListView.getItems().add(p.getMedicineName() + " | Dosage: " + p.getDosage() + " | " + p.getFrequency() + " (" + p.getDurationDays() + " days)");
            }
        } else {
            prescriptionsListView.getItems().add("No prescriptions attached to this visit.");
        }
    }

    public void loadVaccinations() {
        List<Vaccination> list = vaccinationDAO.getAllVaccinations();
        vaccinationTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleAddClinicalRecord() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Clinical Consultation Record");
        dialog.setHeaderText("Record symptoms, diagnosis, and treatment:");

        ButtonType saveBtnType = new ButtonType("Save Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Pet> petCombo = new ComboBox<>();
        petCombo.getItems().addAll(petDAO.getAllPets());
        petCombo.setValue(petSelectorCombo.getValue());
        petCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Veterinarian> vetCombo = new ComboBox<>();
        vetCombo.getItems().addAll(vetDAO.getAllVeterinarians());
        if (!vetCombo.getItems().isEmpty()) vetCombo.setValue(vetCombo.getItems().get(0));
        vetCombo.setMaxWidth(Double.MAX_VALUE);

        DatePicker visitDatePicker = new DatePicker(LocalDate.now());
        TextArea symptomsArea = new TextArea();
        symptomsArea.setPromptText("Observable signs, temperature, pulse...");
        symptomsArea.setPrefRowCount(2);

        TextArea diagnosisArea = new TextArea();
        diagnosisArea.setPromptText("Clinical diagnosis...");
        diagnosisArea.setPrefRowCount(2);

        TextArea treatmentArea = new TextArea();
        treatmentArea.setPromptText("Procedures performed, fluids administered...");
        treatmentArea.setPrefRowCount(2);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Veterinarian advisory notes...");
        notesArea.setPrefRowCount(2);

        grid.add(new Label("Patient Pet: *"), 0, 0);
        grid.add(petCombo, 1, 0);
        grid.add(new Label("Attending Vet: *"), 0, 1);
        grid.add(vetCombo, 1, 1);
        grid.add(new Label("Visit Date: *"), 0, 2);
        grid.add(visitDatePicker, 1, 2);
        grid.add(new Label("Symptoms:"), 0, 3);
        grid.add(symptomsArea, 1, 3);
        grid.add(new Label("Diagnosis: *"), 0, 4);
        grid.add(diagnosisArea, 1, 4);
        grid.add(new Label("Treatment: *"), 0, 5);
        grid.add(treatmentArea, 1, 5);
        grid.add(new Label("Notes:"), 0, 6);
        grid.add(notesArea, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (petCombo.getValue() == null || vetCombo.getValue() == null ||
                    diagnosisArea.getText().trim().isEmpty() || treatmentArea.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please provide Pet, Veterinarian, Diagnosis, and Treatment.");
                    return;
                }

                MedicalRecord rec = new MedicalRecord();
                rec.setPetId(petCombo.getValue().getId());
                rec.setVetId(vetCombo.getValue().getId());
                rec.setVisitDate(Date.valueOf(visitDatePicker.getValue()));
                rec.setSymptoms(symptomsArea.getText().trim());
                rec.setDiagnosis(diagnosisArea.getText().trim());
                rec.setTreatment(treatmentArea.getText().trim());
                rec.setNotes(notesArea.getText().trim());

                if (recordDAO.createRecord(rec)) {
                    AlertUtil.showInfo("Saved", "Clinical record created successfully.");
                    loadRecordsForSelectedPet();
                } else {
                    AlertUtil.showError("Error", "Could not save medical record.");
                }
            }
        });
    }

    @FXML
    public void handleAddPrescription() {
        MedicalRecord selected = recordsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a medical record from the table to prescribe medicines.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Prescribe Medicine");
        dialog.setHeaderText("Add medicine to visit record #" + selected.getId() + " (" + selected.getPetName() + "):");

        ButtonType saveBtnType = new ButtonType("Add Prescription", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Medicine> medCombo = new ComboBox<>();
        medCombo.getItems().addAll(medicineDAO.getAllMedicines());
        medCombo.setMaxWidth(Double.MAX_VALUE);

        TextField dosageField = new TextField("1 tablet");
        TextField freqField = new TextField("Twice daily with meals");
        TextField daysField = new TextField("7");
        TextField instructionsField = new TextField("Keep refrigerated");

        grid.add(new Label("Select Medicine: *"), 0, 0);
        grid.add(medCombo, 1, 0);
        grid.add(new Label("Dosage: *"), 0, 1);
        grid.add(dosageField, 1, 1);
        grid.add(new Label("Frequency: *"), 0, 2);
        grid.add(freqField, 1, 2);
        grid.add(new Label("Duration (Days): *"), 0, 3);
        grid.add(daysField, 1, 3);
        grid.add(new Label("Special Instructions:"), 0, 4);
        grid.add(instructionsField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (medCombo.getValue() == null || dosageField.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please select a medicine and dosage.");
                    return;
                }

                Prescription p = new Prescription();
                p.setMedicalRecordId(selected.getId());
                p.setMedicineId(medCombo.getValue().getId());
                p.setDosage(dosageField.getText().trim());
                p.setFrequency(freqField.getText().trim());
                try {
                    p.setDurationDays(Integer.parseInt(daysField.getText().trim()));
                } catch (Exception e) {
                    p.setDurationDays(7);
                }
                p.setInstructions(instructionsField.getText().trim());

                if (recordDAO.addPrescription(p)) {
                    AlertUtil.showInfo("Prescription Added", "Medicine prescribed successfully.");
                    loadRecordsForSelectedPet();
                } else {
                    AlertUtil.showError("Error", "Could not attach prescription.");
                }
            }
        });
    }

    @FXML
    public void handleAddVaccination() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Pet Vaccination");
        dialog.setHeaderText("Enter vaccination details and booster due date:");

        ButtonType saveBtnType = new ButtonType("Save Vaccination", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Pet> petCombo = new ComboBox<>();
        petCombo.getItems().addAll(petDAO.getAllPets());
        petCombo.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> vaccineCombo = new ComboBox<>();
        vaccineCombo.getItems().addAll(
            "Rabies (Annual)", "DHPP Core (Canine)", "FVRCP Core (Feline)",
            "Bordetella (Kennel Cough)", "Lyme Disease", "Feline Leukemia (FeLV)"
        );
        vaccineCombo.setEditable(true);

        DatePicker adminDatePicker = new DatePicker(LocalDate.now());
        DatePicker nextDuePicker = new DatePicker(LocalDate.now().plusYears(1));
        TextField batchField = new TextField("B-2026-" + ((int)(Math.random()*9000)+1000));

        ComboBox<Veterinarian> vetCombo = new ComboBox<>();
        vetCombo.getItems().addAll(vetDAO.getAllVeterinarians());
        if (!vetCombo.getItems().isEmpty()) vetCombo.setValue(vetCombo.getItems().get(0));

        TextField notesField = new TextField("Administered subcutaneously, no adverse reactions.");

        grid.add(new Label("Select Pet: *"), 0, 0);
        grid.add(petCombo, 1, 0);
        grid.add(new Label("Vaccine Name: *"), 0, 1);
        grid.add(vaccineCombo, 1, 1);
        grid.add(new Label("Administered Date: *"), 0, 2);
        grid.add(adminDatePicker, 1, 2);
        grid.add(new Label("Next Due Date (Booster): *"), 0, 3);
        grid.add(nextDuePicker, 1, 3);
        grid.add(new Label("Batch Number:"), 0, 4);
        grid.add(batchField, 1, 4);
        grid.add(new Label("Administered By:"), 0, 5);
        grid.add(vetCombo, 1, 5);
        grid.add(new Label("Notes:"), 0, 6);
        grid.add(notesField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (petCombo.getValue() == null || vaccineCombo.getValue() == null || adminDatePicker.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please fill in Pet, Vaccine Name, and Administered Date.");
                    return;
                }

                Vaccination v = new Vaccination();
                v.setPetId(petCombo.getValue().getId());
                v.setVaccineName(vaccineCombo.getValue().trim());
                v.setAdministeredDate(Date.valueOf(adminDatePicker.getValue()));
                if (nextDuePicker.getValue() != null) {
                    v.setNextDueDate(Date.valueOf(nextDuePicker.getValue()));
                }
                v.setBatchNumber(batchField.getText().trim());
                if (vetCombo.getValue() != null) {
                    v.setAdministeredByVetId(vetCombo.getValue().getId());
                }
                v.setStatus("GIVEN");
                v.setNotes(notesField.getText().trim());

                if (vaccinationDAO.createVaccination(v)) {
                    AlertUtil.showInfo("Vaccination Logged", "Vaccination record logged successfully.");
                    loadVaccinations();
                } else {
                    AlertUtil.showError("Error", "Could not save vaccination record.");
                }
            }
        });
    }
}

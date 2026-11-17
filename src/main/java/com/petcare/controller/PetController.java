package com.petcare.controller;

import com.petcare.dao.PetDAO;
import com.petcare.dao.PetOwnerDAO;
import com.petcare.model.Pet;
import com.petcare.model.PetOwner;
import com.petcare.util.AlertUtil;
import com.petcare.util.DateUtil;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class PetController {

    // Pets Tab
    @FXML private TableView<Pet> petsTable;
    @FXML private TableColumn<Pet, String> colPetName;
    @FXML private TableColumn<Pet, String> colPetSpecies;
    @FXML private TableColumn<Pet, String> colPetBreed;
    @FXML private TableColumn<Pet, String> colPetGender;
    @FXML private TableColumn<Pet, String> colPetWeight;
    @FXML private TableColumn<Pet, String> colPetOwner;
    @FXML private TableColumn<Pet, String> colPetMicrochip;
    @FXML private TextField searchPetField;

    // Owners Tab
    @FXML private TableView<PetOwner> ownersTable;
    @FXML private TableColumn<PetOwner, String> colOwnerName;
    @FXML private TableColumn<PetOwner, String> colOwnerPhone;
    @FXML private TableColumn<PetOwner, String> colOwnerEmail;
    @FXML private TableColumn<PetOwner, String> colOwnerCity;
    @FXML private TableColumn<PetOwner, String> colOwnerEmergency;
    @FXML private TextField searchOwnerField;

    private final PetDAO petDAO = new PetDAO();
    private final PetOwnerDAO ownerDAO = new PetOwnerDAO();

    @FXML
    public void initialize() {
        setupPetTable();
        setupOwnerTable();
        loadPets();
        loadOwners();

        searchPetField.textProperty().addListener((obs, oldV, newV) -> searchPets());
        searchOwnerField.textProperty().addListener((obs, oldV, newV) -> searchOwners());
    }

    private void setupPetTable() {
        colPetName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPetSpecies.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecies()));
        colPetBreed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBreed()));
        colPetGender.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGender()));
        colPetWeight.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWeightKg() != null ? c.getValue().getWeightKg().toString() + " kg" : "-"));
        colPetOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerName()));
        colPetMicrochip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMicrochipId()));
    }

    private void setupOwnerTable() {
        colOwnerName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colOwnerPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colOwnerEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colOwnerCity.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCity()));
        colOwnerEmergency.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmergencyContact()));
    }

    public void loadPets() {
        List<Pet> list = petDAO.getAllPets();
        petsTable.setItems(FXCollections.observableArrayList(list));
    }

    public void loadOwners() {
        List<PetOwner> list = ownerDAO.getAllOwners();
        ownersTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void searchPets() {
        String keyword = searchPetField.getText().trim();
        if (keyword.isEmpty()) {
            loadPets();
        } else {
            petsTable.setItems(FXCollections.observableArrayList(petDAO.searchPets(keyword)));
        }
    }

    @FXML
    public void searchOwners() {
        String keyword = searchOwnerField.getText().trim();
        if (keyword.isEmpty()) {
            loadOwners();
        } else {
            ownersTable.setItems(FXCollections.observableArrayList(ownerDAO.searchOwners(keyword)));
        }
    }

    @FXML
    public void handleAddPet() {
        showPetFormDialog(null);
    }

    @FXML
    public void handleEditPet() {
        Pet selected = petsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a pet from the table to edit.");
            return;
        }
        showPetFormDialog(selected);
    }

    @FXML
    public void handleDeletePet() {
        Pet selected = petsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a pet from the table to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm Deletion", "Are you sure you want to delete pet: " + selected.getName() + "?")) {
            if (petDAO.deletePet(selected.getId())) {
                AlertUtil.showInfo("Success", "Pet deleted successfully.");
                loadPets();
            } else {
                AlertUtil.showError("Error", "Could not delete pet record.");
            }
        }
    }

    private void showPetFormDialog(Pet existingPet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingPet == null ? "Register New Pet" : "Edit Pet Information");
        dialog.setHeaderText(existingPet == null ? "Enter pet details and select owner:" : "Update pet details:");

        ButtonType saveBtnType = new ButtonType("Save Pet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<PetOwner> ownerCombo = new ComboBox<>();
        ownerCombo.getItems().addAll(ownerDAO.getAllOwners());
        ownerCombo.setMaxWidth(Double.MAX_VALUE);

        TextField nameField = new TextField();
        ComboBox<String> speciesCombo = new ComboBox<>();
        speciesCombo.getItems().addAll("Dog", "Cat", "Bird", "Rabbit", "Reptile", "Other");
        speciesCombo.setEditable(true);

        TextField breedField = new TextField();
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("MALE", "FEMALE");
        genderCombo.setValue("MALE");

        DatePicker dobPicker = new DatePicker();
        TextField weightField = new TextField();
        TextField colorField = new TextField();
        TextField chipField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        if (existingPet != null) {
            nameField.setText(existingPet.getName());
            speciesCombo.setValue(existingPet.getSpecies());
            breedField.setText(existingPet.getBreed());
            genderCombo.setValue(existingPet.getGender());
            if (existingPet.getDateOfBirth() != null) {
                dobPicker.setValue(existingPet.getDateOfBirth().toLocalDate());
            }
            if (existingPet.getWeightKg() != null) {
                weightField.setText(existingPet.getWeightKg().toString());
            }
            colorField.setText(existingPet.getColor());
            chipField.setText(existingPet.getMicrochipId());
            notesArea.setText(existingPet.getAllergiesOrNotes());

            for (PetOwner o : ownerCombo.getItems()) {
                if (o.getId() == existingPet.getOwnerId()) {
                    ownerCombo.setValue(o);
                    break;
                }
            }
        }

        grid.add(new Label("Owner: *"), 0, 0);
        grid.add(ownerCombo, 1, 0);
        grid.add(new Label("Pet Name: *"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Species: *"), 0, 2);
        grid.add(speciesCombo, 1, 2);
        grid.add(new Label("Breed:"), 0, 3);
        grid.add(breedField, 1, 3);
        grid.add(new Label("Gender:"), 0, 4);
        grid.add(genderCombo, 1, 4);
        grid.add(new Label("Date of Birth:"), 0, 5);
        grid.add(dobPicker, 1, 5);
        grid.add(new Label("Weight (kg):"), 0, 6);
        grid.add(weightField, 1, 6);
        grid.add(new Label("Color:"), 0, 7);
        grid.add(colorField, 1, 7);
        grid.add(new Label("Microchip ID:"), 0, 8);
        grid.add(chipField, 1, 8);
        grid.add(new Label("Notes / Allergies:"), 0, 9);
        grid.add(notesArea, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (ownerCombo.getValue() == null || nameField.getText().trim().isEmpty() || speciesCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please fill in all required fields (Owner, Pet Name, Species).");
                    return;
                }

                Pet pet = existingPet != null ? existingPet : new Pet();
                pet.setOwnerId(ownerCombo.getValue().getId());
                pet.setName(nameField.getText().trim());
                pet.setSpecies(speciesCombo.getValue().trim());
                pet.setBreed(breedField.getText().trim());
                pet.setGender(genderCombo.getValue());
                pet.setDateOfBirth(DateUtil.toSqlDate(dobPicker.getValue()));
                try {
                    pet.setWeightKg(new BigDecimal(weightField.getText().trim()));
                } catch (Exception ignored) {}
                pet.setColor(colorField.getText().trim());
                pet.setMicrochipId(chipField.getText().trim());
                pet.setAllergiesOrNotes(notesArea.getText().trim());

                boolean success = existingPet == null ? petDAO.createPet(pet) : petDAO.updatePet(pet);
                if (success) {
                    AlertUtil.showInfo("Success", "Pet record saved successfully.");
                    loadPets();
                } else {
                    AlertUtil.showError("Error", "Could not save pet record.");
                }
            }
        });
    }

    @FXML
    public void handleAddOwner() {
        showOwnerFormDialog(null);
    }

    @FXML
    public void handleEditOwner() {
        PetOwner selected = ownersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an owner from the table to edit.");
            return;
        }
        showOwnerFormDialog(selected);
    }

    @FXML
    public void handleDeleteOwner() {
        PetOwner selected = ownersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an owner from the table to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm Deletion", "Deleting this owner will also remove all associated pets. Proceed?")) {
            if (ownerDAO.deleteOwner(selected.getId())) {
                AlertUtil.showInfo("Success", "Owner record deleted.");
                loadOwners();
                loadPets();
            } else {
                AlertUtil.showError("Error", "Could not delete owner record.");
            }
        }
    }

    private void showOwnerFormDialog(PetOwner existingOwner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingOwner == null ? "Register New Pet Owner" : "Edit Pet Owner Information");
        dialog.setHeaderText(existingOwner == null ? "Enter pet owner details:" : "Update pet owner details:");

        ButtonType saveBtnType = new ButtonType("Save Owner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField addressField = new TextField();
        TextField cityField = new TextField();
        TextField emergencyField = new TextField();

        if (existingOwner != null) {
            firstNameField.setText(existingOwner.getFirstName());
            lastNameField.setText(existingOwner.getLastName());
            emailField.setText(existingOwner.getEmail());
            phoneField.setText(existingOwner.getPhone());
            addressField.setText(existingOwner.getAddress());
            cityField.setText(existingOwner.getCity());
            emergencyField.setText(existingOwner.getEmergencyContact());
        }

        grid.add(new Label("First Name: *"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name: *"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Phone: *"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email: *"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressField, 1, 4);
        grid.add(new Label("City:"), 0, 5);
        grid.add(cityField, 1, 5);
        grid.add(new Label("Emergency Contact:"), 0, 6);
        grid.add(emergencyField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (firstNameField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                    AlertUtil.showError("Validation Error", "Please provide First Name, Phone and Email.");
                    return;
                }

                PetOwner owner = existingOwner != null ? existingOwner : new PetOwner();
                owner.setFirstName(firstNameField.getText().trim());
                owner.setLastName(lastNameField.getText().trim());
                owner.setPhone(phoneField.getText().trim());
                owner.setEmail(emailField.getText().trim());
                owner.setAddress(addressField.getText().trim());
                owner.setCity(cityField.getText().trim());
                owner.setEmergencyContact(emergencyField.getText().trim());

                boolean success = existingOwner == null ? ownerDAO.createOwner(owner) : ownerDAO.updateOwner(owner);
                if (success) {
                    AlertUtil.showInfo("Success", "Owner record saved successfully.");
                    loadOwners();
                } else {
                    AlertUtil.showError("Error", "Could not save owner record.");
                }
            }
        });
    }
}

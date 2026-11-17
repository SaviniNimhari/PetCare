package com.petcare.controller;

import com.petcare.dao.MedicineDAO;
import com.petcare.model.Medicine;
import com.petcare.util.AlertUtil;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class InventoryController {

    @FXML private TableView<Medicine> medicineTable;
    @FXML private TableColumn<Medicine, String> colMedName;
    @FXML private TableColumn<Medicine, String> colCategory;
    @FXML private TableColumn<Medicine, String> colForm;
    @FXML private TableColumn<Medicine, String> colStrength;
    @FXML private TableColumn<Medicine, String> colStock;
    @FXML private TableColumn<Medicine, String> colMinLevel;
    @FXML private TableColumn<Medicine, String> colPrice;
    @FXML private TableColumn<Medicine, String> colExpiry;
    @FXML private TableColumn<Medicine, String> colStatus;

    @FXML private TextField searchField;
    @FXML private CheckBox lowStockFilterCheck;

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        setupTable();
        loadMedicines();

        searchField.textProperty().addListener((obs, oldV, newV) -> handleSearch());
        lowStockFilterCheck.setOnAction(e -> handleSearch());
    }

    private void setupTable() {
        colMedName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colForm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosageForm()));
        colStrength.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStrength()));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantityInStock())));
        colMinLevel.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getMinStockLevel())));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnitPrice() != null ? currencyFormat.format(c.getValue().getUnitPrice()) : "$0.00"));
        colExpiry.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpiryDate() != null ? c.getValue().getExpiryDate().toString() : "-"));
        colStatus.setCellValueFactory(c -> {
            if (c.getValue().isLowStock()) {
                return new SimpleStringProperty("⚠️ Low Stock");
            }
            return new SimpleStringProperty("✅ In Stock");
        });
    }

    public void loadMedicines() {
        List<Medicine> list = lowStockFilterCheck.isSelected() ? medicineDAO.getLowStockMedicines() : medicineDAO.getAllMedicines();
        medicineTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleSearch() {
        String keyword = searchField.getText().trim();
        if (lowStockFilterCheck.isSelected()) {
            medicineTable.setItems(FXCollections.observableArrayList(medicineDAO.getLowStockMedicines()));
        } else if (!keyword.isEmpty()) {
            medicineTable.setItems(FXCollections.observableArrayList(medicineDAO.searchMedicines(keyword)));
        } else {
            loadMedicines();
        }
    }

    @FXML
    public void handleAddMedicine() {
        showMedicineDialog(null);
    }

    @FXML
    public void handleEditMedicine() {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a medicine from the table to edit.");
            return;
        }
        showMedicineDialog(selected);
    }

    @FXML
    public void handleAdjustStock() {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a medicine from the table to adjust its stock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Adjust Stock: " + selected.getName());
        dialog.setHeaderText("Current Stock: " + selected.getQuantityInStock() + " units.\nEnter quantity to add (use negative value to subtract):");
        dialog.setContentText("Quantity Adjustment:");

        dialog.showAndWait().ifPresent(val -> {
            try {
                int change = Integer.parseInt(val.trim());
                if (medicineDAO.updateStock(selected.getId(), change)) {
                    AlertUtil.showInfo("Stock Updated", "Stock successfully updated.");
                    loadMedicines();
                } else {
                    AlertUtil.showError("Error", "Could not adjust stock.");
                }
            } catch (NumberFormatException e) {
                AlertUtil.showError("Invalid Input", "Please enter a valid whole number.");
            }
        });
    }

    @FXML
    public void handleDeleteMedicine() {
        Medicine selected = medicineTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select a medicine to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Delete Medicine", "Are you sure you want to remove " + selected.getName() + " from pharmacy inventory?")) {
            if (medicineDAO.deleteMedicine(selected.getId())) {
                AlertUtil.showInfo("Success", "Medicine removed.");
                loadMedicines();
            } else {
                AlertUtil.showError("Error", "Could not remove medicine.");
            }
        }
    }

    private void showMedicineDialog(Medicine existingMed) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingMed == null ? "Add New Medicine" : "Edit Medicine Details");
        dialog.setHeaderText("Enter pharmaceutical inventory specifications:");

        ButtonType saveBtnType = new ButtonType("Save Medicine", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("Antibiotic", "Anti-inflammatory", "Vaccine", "Parasiticide", "Corticosteroid", "Analgesic", "Dermatology", "Other");
        catCombo.setEditable(true);

        ComboBox<String> formCombo = new ComboBox<>();
        formCombo.getItems().addAll("Tablet", "Syrup", "Injectable", "Oral Suspension", "Chewable", "Ointment", "Liquid Rinse");
        formCombo.setEditable(true);

        TextField strengthField = new TextField("250mg");
        TextField stockField = new TextField("50");
        TextField minStockField = new TextField("10");
        TextField priceField = new TextField("15.00");
        DatePicker expiryPicker = new DatePicker(LocalDate.now().plusYears(1));
        TextField mfgField = new TextField();

        if (existingMed != null) {
            nameField.setText(existingMed.getName());
            catCombo.setValue(existingMed.getCategory());
            formCombo.setValue(existingMed.getDosageForm());
            strengthField.setText(existingMed.getStrength());
            stockField.setText(String.valueOf(existingMed.getQuantityInStock()));
            minStockField.setText(String.valueOf(existingMed.getMinStockLevel()));
            if (existingMed.getUnitPrice() != null) priceField.setText(existingMed.getUnitPrice().toString());
            if (existingMed.getExpiryDate() != null) expiryPicker.setValue(existingMed.getExpiryDate().toLocalDate());
            mfgField.setText(existingMed.getManufacturer());
        }

        grid.add(new Label("Medicine Name: *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Category: *"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Dosage Form: *"), 0, 2);
        grid.add(formCombo, 1, 2);
        grid.add(new Label("Strength:"), 0, 3);
        grid.add(strengthField, 1, 3);
        grid.add(new Label("Initial Stock: *"), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(new Label("Min Stock Alert Level: *"), 0, 5);
        grid.add(minStockField, 1, 5);
        grid.add(new Label("Unit Price ($): *"), 0, 6);
        grid.add(priceField, 1, 6);
        grid.add(new Label("Expiry Date: *"), 0, 7);
        grid.add(expiryPicker, 1, 7);
        grid.add(new Label("Manufacturer:"), 0, 8);
        grid.add(mfgField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveBtnType) {
                if (nameField.getText().trim().isEmpty() || catCombo.getValue() == null || formCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please fill in all mandatory medicine attributes.");
                    return;
                }

                Medicine m = existingMed != null ? existingMed : new Medicine();
                m.setName(nameField.getText().trim());
                m.setCategory(catCombo.getValue().trim());
                m.setDosageForm(formCombo.getValue().trim());
                m.setStrength(strengthField.getText().trim());
                try {
                    m.setQuantityInStock(Integer.parseInt(stockField.getText().trim()));
                    m.setMinStockLevel(Integer.parseInt(minStockField.getText().trim()));
                    m.setUnitPrice(new BigDecimal(priceField.getText().trim()));
                } catch (Exception e) {
                    AlertUtil.showError("Format Error", "Stock and Price must be numeric.");
                    return;
                }
                if (expiryPicker.getValue() != null) {
                    m.setExpiryDate(Date.valueOf(expiryPicker.getValue()));
                }
                m.setManufacturer(mfgField.getText().trim());

                boolean success = existingMed == null ? medicineDAO.createMedicine(m) : medicineDAO.updateMedicine(m);
                if (success) {
                    AlertUtil.showInfo("Saved", "Medicine record successfully saved.");
                    loadMedicines();
                } else {
                    AlertUtil.showError("Error", "Could not save medicine record.");
                }
            }
        });
    }
}

package com.petcare.controller;

import com.petcare.dao.InvoiceDAO;
import com.petcare.dao.PetDAO;
import com.petcare.dao.PetOwnerDAO;
import com.petcare.model.Invoice;
import com.petcare.model.Payment;
import com.petcare.model.Pet;
import com.petcare.model.PetOwner;
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

public class BillingController {

    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, String> colInvoiceNum;
    @FXML private TableColumn<Invoice, String> colDate;
    @FXML private TableColumn<Invoice, String> colPet;
    @FXML private TableColumn<Invoice, String> colOwner;
    @FXML private TableColumn<Invoice, String> colTotal;
    @FXML private TableColumn<Invoice, String> colStatus;

    @FXML private ComboBox<String> statusFilterCombo;

    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PetDAO petDAO = new PetDAO();
    private final PetOwnerDAO ownerDAO = new PetOwnerDAO();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @FXML
    public void initialize() {
        setupTable();
        statusFilterCombo.getItems().addAll("ALL", "PENDING", "PAID", "CANCELLED");
        statusFilterCombo.setValue("ALL");
        statusFilterCombo.setOnAction(e -> loadInvoices());

        loadInvoices();
    }

    private void setupTable() {
        colInvoiceNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceNumber()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceDate() != null ? c.getValue().getInvoiceDate().toString() : "-"));
        colPet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPetName()));
        colOwner.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOwnerName()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotalAmount() != null ? currencyFormat.format(c.getValue().getTotalAmount()) : "$0.00"));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    public void loadInvoices() {
        List<Invoice> list = invoiceDAO.getAllInvoices();
        String filter = statusFilterCombo.getValue();
        if (filter != null && !"ALL".equalsIgnoreCase(filter)) {
            list = list.stream().filter(i -> i.getStatus().equalsIgnoreCase(filter)).toList();
        }
        invoiceTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleCreateInvoice() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generate New Clinic Invoice");
        dialog.setHeaderText("Compute fees and generate invoice:");

        ButtonType generateBtn = new ButtonType("Generate Invoice", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ComboBox<Pet> petCombo = new ComboBox<>();
        petCombo.getItems().addAll(petDAO.getAllPets());
        petCombo.setMaxWidth(Double.MAX_VALUE);

        TextField consultFeeField = new TextField("60.00");
        TextField treatmentFeeField = new TextField("30.00");
        TextField medicineFeeField = new TextField("25.00");
        TextField discountField = new TextField("0.00");
        TextField taxField = new TextField("5.75");
        Label totalPreviewLabel = new Label("$120.75");
        totalPreviewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0D9488;");

        Runnable calcTotal = () -> {
            try {
                BigDecimal c = new BigDecimal(consultFeeField.getText().trim());
                BigDecimal tr = new BigDecimal(treatmentFeeField.getText().trim());
                BigDecimal m = new BigDecimal(medicineFeeField.getText().trim());
                BigDecimal d = new BigDecimal(discountField.getText().trim());
                BigDecimal tx = new BigDecimal(taxField.getText().trim());
                BigDecimal sub = c.add(tr).add(m).subtract(d);
                if (sub.compareTo(BigDecimal.ZERO) < 0) sub = BigDecimal.ZERO;
                BigDecimal tot = sub.add(tx);
                totalPreviewLabel.setText(currencyFormat.format(tot));
            } catch (Exception ignored) {}
        };

        consultFeeField.textProperty().addListener((o, oldV, newV) -> calcTotal.run());
        treatmentFeeField.textProperty().addListener((o, oldV, newV) -> calcTotal.run());
        medicineFeeField.textProperty().addListener((o, oldV, newV) -> calcTotal.run());
        discountField.textProperty().addListener((o, oldV, newV) -> calcTotal.run());
        taxField.textProperty().addListener((o, oldV, newV) -> calcTotal.run());

        grid.add(new Label("Select Patient Pet: *"), 0, 0);
        grid.add(petCombo, 1, 0);
        grid.add(new Label("Consultation Fee ($):"), 0, 1);
        grid.add(consultFeeField, 1, 1);
        grid.add(new Label("Treatment / Procedure Fee ($):"), 0, 2);
        grid.add(treatmentFeeField, 1, 2);
        grid.add(new Label("Medicine Total ($):"), 0, 3);
        grid.add(medicineFeeField, 1, 3);
        grid.add(new Label("Discount ($):"), 0, 4);
        grid.add(discountField, 1, 4);
        grid.add(new Label("Taxes ($):"), 0, 5);
        grid.add(taxField, 1, 5);
        grid.add(new Label("Total Invoice Amount:"), 0, 6);
        grid.add(totalPreviewLabel, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == generateBtn) {
                if (petCombo.getValue() == null) {
                    AlertUtil.showError("Validation Error", "Please select a pet.");
                    return;
                }

                Invoice inv = new Invoice();
                inv.setPetId(petCombo.getValue().getId());
                inv.setOwnerId(petCombo.getValue().getOwnerId());
                inv.setInvoiceDate(Date.valueOf(LocalDate.now()));
                try {
                    inv.setConsultationFee(new BigDecimal(consultFeeField.getText().trim()));
                    inv.setTreatmentFee(new BigDecimal(treatmentFeeField.getText().trim()));
                    inv.setMedicineTotal(new BigDecimal(medicineFeeField.getText().trim()));
                    inv.setDiscountAmount(new BigDecimal(discountField.getText().trim()));
                    inv.setTaxAmount(new BigDecimal(taxField.getText().trim()));
                    inv.recalculateTotal();
                } catch (Exception e) {
                    AlertUtil.showError("Format Error", "All fees must be valid numbers.");
                    return;
                }

                inv.setStatus("PENDING");
                if (invoiceDAO.createInvoice(inv)) {
                    AlertUtil.showInfo("Invoice Created", "Invoice " + inv.getInvoiceNumber() + " successfully generated.");
                    loadInvoices();
                } else {
                    AlertUtil.showError("Error", "Could not generate invoice.");
                }
            }
        });
    }

    @FXML
    public void handleRecordPayment() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an invoice from the table to record a payment.");
            return;
        }

        if ("PAID".equalsIgnoreCase(selected.getStatus())) {
            AlertUtil.showInfo("Already Settled", "This invoice is already marked as PAID.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Payment for " + selected.getInvoiceNumber());
        dialog.setHeaderText("Total Payable: " + currencyFormat.format(selected.getTotalAmount()));

        ButtonType payBtn = new ButtonType("Confirm Payment", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(payBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField amountField = new TextField(selected.getTotalAmount().toString());
        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "DIGITAL_WALLET");
        methodCombo.setValue("CREDIT_CARD");

        TextField refField = new TextField("TXN-" + System.currentTimeMillis() % 1000000);
        TextField notesField = new TextField("Front desk checkout");

        grid.add(new Label("Amount Paid ($): *"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Payment Method: *"), 0, 1);
        grid.add(methodCombo, 1, 1);
        grid.add(new Label("Transaction Reference:"), 0, 2);
        grid.add(refField, 1, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == payBtn) {
                Payment p = new Payment();
                p.setInvoiceId(selected.getId());
                p.setPaymentDate(Date.valueOf(LocalDate.now()));
                try {
                    p.setAmount(new BigDecimal(amountField.getText().trim()));
                } catch (Exception e) {
                    AlertUtil.showError("Error", "Amount must be numeric.");
                    return;
                }
                p.setPaymentMethod(methodCombo.getValue());
                p.setTransactionReference(refField.getText().trim());
                p.setNotes(notesField.getText().trim());

                if (invoiceDAO.recordPayment(p)) {
                    AlertUtil.showInfo("Payment Recorded", "Payment of " + currencyFormat.format(p.getAmount()) + " logged. Invoice marked as PAID.");
                    loadInvoices();
                } else {
                    AlertUtil.showError("Error", "Could not record payment.");
                }
            }
        });
    }

    @FXML
    public void handleViewInvoice() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Selection Required", "Please select an invoice from the table.");
            return;
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("=========================================\n");
        receipt.append("          🐾 PETCARE VETERINARY CLINIC\n");
        receipt.append("=========================================\n\n");
        receipt.append("Invoice No:   ").append(selected.getInvoiceNumber()).append("\n");
        receipt.append("Date:         ").append(selected.getInvoiceDate()).append("\n");
        receipt.append("Patient:      ").append(selected.getPetName()).append("\n");
        receipt.append("Owner:        ").append(selected.getOwnerName()).append("\n");
        receipt.append("Status:       ").append(selected.getStatus()).append("\n\n");
        receipt.append("-----------------------------------------\n");
        receipt.append("Consultation Fee:      ").append(currencyFormat.format(selected.getConsultationFee())).append("\n");
        receipt.append("Treatment / Procedure: ").append(currencyFormat.format(selected.getTreatmentFee())).append("\n");
        receipt.append("Pharmacy & Medicines:  ").append(currencyFormat.format(selected.getMedicineTotal())).append("\n");
        receipt.append("Discount:             -").append(currencyFormat.format(selected.getDiscountAmount())).append("\n");
        receipt.append("Taxes:                +").append(currencyFormat.format(selected.getTaxAmount())).append("\n");
        receipt.append("-----------------------------------------\n");
        receipt.append("TOTAL AMOUNT:          ").append(currencyFormat.format(selected.getTotalAmount())).append("\n");
        receipt.append("=========================================\n");
        receipt.append("Thank you for choosing PetCare Clinic!\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invoice Receipt - " + selected.getInvoiceNumber());
        alert.setHeaderText("PetCare Official Invoice Summary");
        TextArea area = new TextArea(receipt.toString());
        area.setEditable(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        area.setPrefSize(420, 360);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
}

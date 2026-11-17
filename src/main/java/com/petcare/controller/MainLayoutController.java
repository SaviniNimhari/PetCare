package com.petcare.controller;

import com.petcare.config.SessionManager;
import com.petcare.model.Role;
import com.petcare.model.User;
import com.petcare.util.AlertUtil;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleBadge;
    @FXML private Label currentPageTitle;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavPets;
    @FXML private Button btnNavAppointments;
    @FXML private Button btnNavVets;
    @FXML private Button btnNavRecords;
    @FXML private Button btnNavInventory;
    @FXML private Button btnNavBilling;
    @FXML private Button btnNavReports;

    private Button currentActiveBtn;

    @FXML
    public void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userFullNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
            userRoleBadge.setText(user.getRole().getDisplayName().toUpperCase());
            applyRolePermissions(user.getRole());
        }

        // Load dashboard by default
        showDashboard();
    }

    private void applyRolePermissions(Role role) {
        if (role == Role.PET_OWNER) {
            // Pet owners only have access to view pets, appointments, and medical history
            btnNavVets.setVisible(false);
            btnNavVets.setManaged(false);
            btnNavInventory.setVisible(false);
            btnNavInventory.setManaged(false);
            btnNavReports.setVisible(false);
            btnNavReports.setManaged(false);
        } else if (role == Role.RECEPTIONIST) {
            // Receptionists manage owners, pets, appointments, billing
            btnNavReports.setVisible(false);
            btnNavReports.setManaged(false);
        } else if (role == Role.VETERINARIAN) {
            // Veterinarians manage appointments, medical records, inventory view
            btnNavBilling.setVisible(false);
            btnNavBilling.setManaged(false);
        }
    }

    private void setActiveButton(Button btn, String title) {
        if (currentActiveBtn != null) {
            currentActiveBtn.getStyleClass().remove("nav-button-active");
        }
        btn.getStyleClass().add("nav-button-active");
        currentActiveBtn = btn;
        currentPageTitle.setText(title);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("View Error", "Could not load view " + fxmlPath + ": " + e.getMessage());
        }
    }

    @FXML
    public void showDashboard() {
        setActiveButton(btnNavDashboard, "Dashboard Overview");
        loadView("/com/petcare/view/Dashboard.fxml");
    }

    @FXML
    public void showPets() {
        setActiveButton(btnNavPets, "Pets & Owners Management");
        loadView("/com/petcare/view/Pets.fxml");
    }

    @FXML
    public void showAppointments() {
        setActiveButton(btnNavAppointments, "Appointment Scheduling");
        loadView("/com/petcare/view/Appointments.fxml");
    }

    @FXML
    public void showVeterinarians() {
        setActiveButton(btnNavVets, "Veterinarian Directory");
        loadView("/com/petcare/view/Veterinarians.fxml");
    }

    @FXML
    public void showMedicalRecords() {
        setActiveButton(btnNavRecords, "Medical Records & Vaccinations");
        loadView("/com/petcare/view/MedicalRecords.fxml");
    }

    @FXML
    public void showInventory() {
        setActiveButton(btnNavInventory, "Medicines & Inventory");
        loadView("/com/petcare/view/Inventory.fxml");
    }

    @FXML
    public void showBilling() {
        setActiveButton(btnNavBilling, "Billing & Invoices");
        loadView("/com/petcare/view/Billing.fxml");
    }

    @FXML
    public void showReports() {
        setActiveButton(btnNavReports, "Reports & Analytics");
        loadView("/com/petcare/view/Reports.fxml");
    }

    @FXML
    public void handleLogout() {
        if (AlertUtil.showConfirmation("Logout Confirmation", "Are you sure you want to log out of PetCare?")) {
            SessionManager.getInstance().logout();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petcare/view/Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setTitle("PetCare - Login");
                stage.setScene(new Scene(root, 900, 600));
                stage.centerOnScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

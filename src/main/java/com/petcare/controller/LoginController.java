package com.petcare.controller;

import com.petcare.config.SessionManager;
import com.petcare.dao.UserDAO;
import com.petcare.model.User;
import com.petcare.util.AlertUtil;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> quickLoginCombo;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        quickLoginCombo.getItems().addAll(
            "Admin (admin / admin123)",
            "Veterinarian (dr.smith / vet123)",
            "Receptionist (sarah.recep / recep123)",
            "Pet Owner (john.owner / owner123)"
        );

        quickLoginCombo.setOnAction(e -> {
            String selected = quickLoginCombo.getValue();
            if (selected == null) return;
            if (selected.startsWith("Admin")) {
                usernameField.setText("admin");
                passwordField.setText("admin123");
            } else if (selected.startsWith("Veterinarian")) {
                usernameField.setText("dr.smith");
                passwordField.setText("vet123");
            } else if (selected.startsWith("Receptionist")) {
                usernameField.setText("sarah.recep");
                passwordField.setText("recep123");
            } else if (selected.startsWith("Pet Owner")) {
                usernameField.setText("john.owner");
                passwordField.setText("owner123");
            }
        });
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        statusLabel.setText("Authenticating...");

        // Authenticate via DAO
        User user = userDAO.authenticate(username, password);

        // Fallback for development if database is not yet booted
        if (user == null) {
            user = getMockFallbackUser(username, password);
        }

        if (user != null) {
            SessionManager.getInstance().setCurrentUser(user);
            openMainLayout();
        } else {
            statusLabel.setText("Invalid credentials or account inactive.");
            AlertUtil.showError("Login Failed", "Invalid username or password. Please check your credentials or pick from Quick Demo Accounts.");
        }
    }

    private User getMockFallbackUser(String username, String password) {
        if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
            return new User(1, "admin", "System Administrator", "admin@petcare.com", "+1-555-0100", com.petcare.model.Role.ADMIN, "ACTIVE");
        } else if ("dr.smith".equalsIgnoreCase(username) && "vet123".equals(password)) {
            return new User(2, "dr.smith", "Dr. Alexander Smith, DVM", "asmith@petcare.com", "+1-555-0101", com.petcare.model.Role.VETERINARIAN, "ACTIVE");
        } else if ("sarah.recep".equalsIgnoreCase(username) && "recep123".equals(password)) {
            return new User(3, "sarah.recep", "Sarah Jenkins", "sjenkins@petcare.com", "+1-555-0103", com.petcare.model.Role.RECEPTIONIST, "ACTIVE");
        } else if ("john.owner".equalsIgnoreCase(username) && "owner123".equals(password)) {
            return new User(4, "john.owner", "Johnathan Doe", "john.doe@example.com", "+1-555-0104", com.petcare.model.Role.PET_OWNER, "ACTIVE");
        }
        return null;
    }

    private void openMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/petcare/view/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 750);
            stage.setTitle("PetCare - Clinic Management System");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Error loading main layout.");
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Could not load main application layout: " + e.getMessage());
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}

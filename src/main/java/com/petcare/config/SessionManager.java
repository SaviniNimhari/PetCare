package com.petcare.config;

import com.petcare.model.Role;
import com.petcare.model.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isVeterinarian() {
        return hasRole(Role.VETERINARIAN);
    }

    public boolean isReceptionist() {
        return hasRole(Role.RECEPTIONIST);
    }

    public boolean isPetOwner() {
        return hasRole(Role.PET_OWNER);
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    public boolean canManageVeterinarians() {
        return isAdmin();
    }

    public boolean canManageMedicalRecords() {
        return isAdmin() || isVeterinarian();
    }

    public boolean canManageBilling() {
        return isAdmin() || isReceptionist();
    }

    public boolean canBookAppointments() {
        return isAdmin() || isReceptionist() || isPetOwner();
    }
}

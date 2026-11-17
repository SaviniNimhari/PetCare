package com.petcare.model;

public enum Role {
    ADMIN("Admin"),
    VETERINARIAN("Veterinarian"),
    RECEPTIONIST("Receptionist"),
    PET_OWNER("Pet Owner");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return RECEPTIONIST;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(roleStr) || r.displayName.equalsIgnoreCase(roleStr)) {
                return r;
            }
        }
        return RECEPTIONIST;
    }
}

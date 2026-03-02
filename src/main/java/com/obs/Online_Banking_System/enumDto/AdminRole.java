package com.obs.Online_Banking_System.enumDto;

public enum AdminRole {
    MANAGER("MANAGER"),
    ADMINISTRATIVE("ADMINISTRATIVE");

    private final String displayType;

    AdminRole(String displayType) {
        this.displayType = displayType;
    }

    public String getType() {
        return displayType;
    }

    public String getDisplayType() {
        return displayType;
    }

    @Override
    public String toString() {
        return displayType;
    }
}

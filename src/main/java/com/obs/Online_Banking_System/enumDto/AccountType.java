package com.obs.Online_Banking_System.enumDto;

public enum AccountType {
    SAVINGS("SAVINGS"),
    CURRENT("CURRENT");

    private final String displayType;

    AccountType(String displayType) {
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

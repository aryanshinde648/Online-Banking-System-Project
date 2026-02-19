package com.obs.Online_Banking_System.enumDto;

public enum TransactionType {
    DEPOSIT("DEPOSIT"),
    WITHDRAW("WITHDRAW"),
    TRANSFER("TRANSFER");

    private final String displayType;

    TransactionType(String displayType) {
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

package com.ITSA.transaction.enums;


public enum TransactionStatus {
    CREATED(0),
    PENDING_APPROVAL(1),
    CHECKER_NOTIFIED(2),
    APPROVED(3),
    REJECTED(4),
    AUTOMATIC_APPROVAL(5),
    AUTOMATIC_REJECTION(6),
    COMPLETED(7);

    private final int value;

    TransactionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TransactionStatus valueOf(int value) {
        for (TransactionStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid TransactionStatus value: " + value);
    }
}

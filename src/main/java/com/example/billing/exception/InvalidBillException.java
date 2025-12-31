package com.example.billing.exception;

public class InvalidBillException extends RuntimeException {

    public InvalidBillException(String message) {
        super(message);
    }

    public InvalidBillException(String message, Throwable cause) {
        super(message, cause);
    }
}

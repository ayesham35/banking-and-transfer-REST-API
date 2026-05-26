package com.example.bankapi.exception;

public class InvalidTransferException extends RuntimeException{
    public InvalidTransferException(String message) {
        super(message);
    }
}

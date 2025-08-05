package com.example.springcrud.exception;

public class UserOperationException extends RuntimeException {

    public UserOperationException(String message) {
        super(message);
    }
}

package com.esgi.blindTest.domain.exception;

public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException() {
        super("Un compte existe déjà pour cet email.");
    }
}

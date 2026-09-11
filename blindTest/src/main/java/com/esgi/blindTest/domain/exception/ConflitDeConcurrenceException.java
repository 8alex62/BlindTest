package com.esgi.blindTest.domain.exception;

public class ConflitDeConcurrenceException extends RuntimeException {

    public ConflitDeConcurrenceException() {
        super("Ce blind test a été modifié entre-temps, merci de réessayer.");
    }
}

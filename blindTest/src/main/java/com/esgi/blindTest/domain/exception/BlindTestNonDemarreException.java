package com.esgi.blindTest.domain.exception;

public class BlindTestNonDemarreException extends RuntimeException {

    public BlindTestNonDemarreException() {
        super("Ce blind test n'est pas en cours.");
    }
}

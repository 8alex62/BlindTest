package com.esgi.blindTest.domain.exception;

public class BlindTestCompletException extends RuntimeException {

    public BlindTestCompletException() {
        super("Ce blind test accueille déjà trois participants.");
    }
}

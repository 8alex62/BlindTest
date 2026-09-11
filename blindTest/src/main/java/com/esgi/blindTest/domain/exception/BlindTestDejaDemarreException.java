package com.esgi.blindTest.domain.exception;

public class BlindTestDejaDemarreException extends RuntimeException {

    public BlindTestDejaDemarreException() {
        super("Ce blind test a déjà démarré.");
    }
}

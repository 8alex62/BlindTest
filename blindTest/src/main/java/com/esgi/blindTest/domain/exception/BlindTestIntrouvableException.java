package com.esgi.blindTest.domain.exception;

public class BlindTestIntrouvableException extends RuntimeException {

    public BlindTestIntrouvableException() {
        super("Ce blind test est introuvable.");
    }
}

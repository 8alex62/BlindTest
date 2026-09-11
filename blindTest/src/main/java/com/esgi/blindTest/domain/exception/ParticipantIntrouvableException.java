package com.esgi.blindTest.domain.exception;

public class ParticipantIntrouvableException extends RuntimeException {

    public ParticipantIntrouvableException() {
        super("Ce participant est introuvable.");
    }
}

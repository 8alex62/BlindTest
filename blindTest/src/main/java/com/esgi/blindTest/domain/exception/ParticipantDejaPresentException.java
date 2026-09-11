package com.esgi.blindTest.domain.exception;

public class ParticipantDejaPresentException extends RuntimeException {

    public ParticipantDejaPresentException() {
        super("Vous participez déjà à ce blind test.");
    }
}

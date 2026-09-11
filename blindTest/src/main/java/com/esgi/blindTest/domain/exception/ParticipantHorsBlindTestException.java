package com.esgi.blindTest.domain.exception;

public class ParticipantHorsBlindTestException extends RuntimeException {

    public ParticipantHorsBlindTestException() {
        super("Vous ne participez pas à ce blind test.");
    }
}

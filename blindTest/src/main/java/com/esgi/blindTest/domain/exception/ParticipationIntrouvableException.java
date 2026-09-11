package com.esgi.blindTest.domain.exception;

public class ParticipationIntrouvableException extends RuntimeException {

    public ParticipationIntrouvableException() {
        super("Cette participation est introuvable.");
    }
}

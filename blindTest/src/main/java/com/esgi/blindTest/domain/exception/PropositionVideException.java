package com.esgi.blindTest.domain.exception;

public class PropositionVideException extends RuntimeException {

    public PropositionVideException() {
        super("La proposition ne peut pas être vide.");
    }
}

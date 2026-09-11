package com.esgi.blindTest.domain.exception;

public class BlindTestTermineException extends RuntimeException {

    public BlindTestTermineException() {
        super("Ce blind test est terminé, les scores sont figés.");
    }
}

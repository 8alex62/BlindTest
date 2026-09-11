package com.esgi.blindTest.domain.exception;

public class BlindTestIncompletException extends RuntimeException {

    public BlindTestIncompletException() {
        super("Un blind test démarre lorsqu'il atteint trois participants.");
    }
}

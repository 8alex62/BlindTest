package com.esgi.blindTest.domain.exception;

public class MorceauIntrouvableException extends RuntimeException {

    public MorceauIntrouvableException() {
        super("Ce morceau est introuvable au catalogue.");
    }
}

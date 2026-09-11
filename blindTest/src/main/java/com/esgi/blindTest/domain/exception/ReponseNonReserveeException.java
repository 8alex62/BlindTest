package com.esgi.blindTest.domain.exception;

public class ReponseNonReserveeException extends RuntimeException {

    public ReponseNonReserveeException() {
        super("Vous devez d'abord cliquer sur « J'ai trouvé » pour proposer un titre.");
    }
}

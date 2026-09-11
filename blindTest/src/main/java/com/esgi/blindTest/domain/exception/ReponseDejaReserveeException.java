package com.esgi.blindTest.domain.exception;

public class ReponseDejaReserveeException extends RuntimeException {

    public ReponseDejaReserveeException() {
        super("Un autre participant a déjà pris la main sur ce morceau.");
    }
}

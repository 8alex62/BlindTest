package com.esgi.blindTest.domain.exception;

import com.esgi.blindTest.domain.model.BlindTest;

public class NombreDeMorceauxInvalideException extends RuntimeException {

    public NombreDeMorceauxInvalideException(int nombreRecu) {
        super("Un blind test compte exactement " + BlindTest.NOMBRE_DE_MORCEAUX
                + " morceaux, " + nombreRecu + " reçu(s).");
    }
}

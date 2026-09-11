package com.esgi.blindTest.presentation.controller.rest;

import com.esgi.blindTest.domain.exception.BlindTestCompletException;
import com.esgi.blindTest.domain.exception.BlindTestDejaDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestIncompletException;
import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.exception.BlindTestNonDemarreException;
import com.esgi.blindTest.domain.exception.BlindTestTermineException;
import com.esgi.blindTest.domain.exception.ConflitDeConcurrenceException;
import com.esgi.blindTest.domain.exception.EmailDejaUtiliseException;
import com.esgi.blindTest.domain.exception.IdentifiantsInvalidesException;
import com.esgi.blindTest.domain.exception.NombreDeMorceauxInvalideException;
import com.esgi.blindTest.domain.exception.ParticipantDejaPresentException;
import com.esgi.blindTest.domain.exception.ParticipantHorsBlindTestException;
import com.esgi.blindTest.domain.exception.ParticipantIntrouvableException;
import com.esgi.blindTest.domain.exception.ParticipationIntrouvableException;
import com.esgi.blindTest.domain.exception.PropositionVideException;
import com.esgi.blindTest.domain.exception.ReponseDejaReserveeException;
import com.esgi.blindTest.domain.exception.ReponseNonReserveeException;
import com.esgi.blindTest.presentation.response.ErreurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions du domaine en codes HTTP. Aucune regle metier ici,
 * seulement une correspondance.
 */
@RestControllerAdvice
public class GestionnaireDExceptions {

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErreurResponse identifiantsInvalides(RuntimeException exception) {
        return new ErreurResponse(exception.getMessage());
    }

    @ExceptionHandler(ParticipantHorsBlindTestException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErreurResponse acces(RuntimeException exception) {
        return new ErreurResponse(exception.getMessage());
    }

    @ExceptionHandler({BlindTestIntrouvableException.class,
            ParticipantIntrouvableException.class,
            ParticipationIntrouvableException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErreurResponse introuvable(RuntimeException exception) {
        return new ErreurResponse(exception.getMessage());
    }

    @ExceptionHandler({ReponseDejaReserveeException.class,
            ReponseNonReserveeException.class,
            BlindTestCompletException.class,
            BlindTestDejaDemarreException.class,
            BlindTestIncompletException.class,
            BlindTestNonDemarreException.class,
            BlindTestTermineException.class,
            ParticipantDejaPresentException.class,
            EmailDejaUtiliseException.class,
            ConflitDeConcurrenceException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErreurResponse conflit(RuntimeException exception) {
        return new ErreurResponse(exception.getMessage());
    }

    @ExceptionHandler({NombreDeMorceauxInvalideException.class, PropositionVideException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErreurResponse requeteInvalide(RuntimeException exception) {
        return new ErreurResponse(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErreurResponse validation(MethodArgumentNotValidException exception) {
        return new ErreurResponse("Les informations envoyees sont invalides.");
    }
}

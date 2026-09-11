package com.esgi.blindTest.presentation.response;

import com.esgi.blindTest.domain.model.EtatLecture;
import com.esgi.blindTest.domain.model.StatutBlindTest;

import java.util.List;

/**
 * Etat publie au navigateur. Le titre du morceau courant n'y figure jamais :
 * c'est la reponse a trouver.
 */
public record EtatBlindTestResponse(Long id,
                                    String nom,
                                    StatutBlindTest statut,
                                    EtatLecture etatLecture,
                                    int numeroDuMorceau,
                                    int nombreDeMorceaux,
                                    String urlAudio,
                                    boolean reponseReservee,
                                    boolean vousAvezLaMain,
                                    List<ScoreParticipant> scores) {
}

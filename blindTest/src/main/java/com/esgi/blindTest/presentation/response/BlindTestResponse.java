package com.esgi.blindTest.presentation.response;

import com.esgi.blindTest.domain.model.StatutBlindTest;

/**
 * Le nom tient lieu de cle : les modeles du domaine n'ont pas d'identifiant.
 */
public record BlindTestResponse(String nom,
                                StatutBlindTest statut,
                                int nombreDeParticipants,
                                int nombreMaximumDeParticipants) {
}

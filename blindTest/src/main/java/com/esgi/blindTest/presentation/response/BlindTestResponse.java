package com.esgi.blindTest.presentation.response;

import com.esgi.blindTest.domain.model.StatutBlindTest;

public record BlindTestResponse(Long id,
                                String nom,
                                StatutBlindTest statut,
                                int nombreDeParticipants,
                                int nombreMaximumDeParticipants) {
}

package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.Participation;

/**
 * Port de persistance des participations.
 */
public interface ParticipationRepository {

    Participation enregistrer(Long idBlindTest, Participation participation);

    Participation ajouterUnPoint(Participation participation);
}

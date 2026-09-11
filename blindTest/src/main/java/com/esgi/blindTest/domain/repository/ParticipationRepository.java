package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.Participation;

/**
 * Port de persistance des participations.
 */
public interface ParticipationRepository {

    Participation ajouterUnPoint(Participation participation);
}

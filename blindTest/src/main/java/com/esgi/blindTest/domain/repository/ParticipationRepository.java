package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.Participation;

/**
 * Port de persistance des participations. Une participation est designee par le nom
 * du blind test et l'email du participant.
 */
public interface ParticipationRepository {

    Participation enregistrer(String nomBlindTest, Participation participation);

    Participation ajouterUnPoint(String nomBlindTest, Participation participation);
}

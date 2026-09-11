package com.esgi.blindTest.domain.repository;

import com.esgi.blindTest.domain.model.Participant;

import java.util.Optional;

/**
 * Port de persistance des participants. Ne manipule que des objets du domaine.
 */
public interface ParticipantRepository {

    Participant save(Participant participant);

    Participant findByEmailAndMotDePasse(String email, String motDePasse);

    Optional<Participant> findByEmail(String email);
}

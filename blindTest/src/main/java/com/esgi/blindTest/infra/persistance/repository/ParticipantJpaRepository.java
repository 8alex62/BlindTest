package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.infra.persistance.entity.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantJpaRepository extends JpaRepository<ParticipantEntity, Long> {

    // Methodes derivees
    ParticipantEntity findByEmailAndMotDePasse(String email, String motDePasse);

    Optional<ParticipantEntity> findByEmail(String email);
}

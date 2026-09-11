package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.infra.persistance.entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipationJpaRepository extends JpaRepository<ParticipationEntity, Long> {

    // Une participation est designee par le nom du blind test et l'email du participant.
    Optional<ParticipationEntity> findByBlindTestNomAndParticipantEmail(String nom, String email);
}

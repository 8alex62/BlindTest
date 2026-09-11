package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.infra.persistance.entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipationJpaRepository extends JpaRepository<ParticipationEntity, Long> {
}

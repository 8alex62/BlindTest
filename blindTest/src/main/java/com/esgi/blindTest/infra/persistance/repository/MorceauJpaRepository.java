package com.esgi.blindTest.infra.persistance.repository;

import com.esgi.blindTest.infra.persistance.entity.MorceauEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MorceauJpaRepository extends JpaRepository<MorceauEntity, Long> {

    Optional<MorceauEntity> findByNom(String nom);
}

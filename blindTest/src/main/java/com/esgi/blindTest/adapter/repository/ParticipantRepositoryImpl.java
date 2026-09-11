package com.esgi.blindTest.adapter.repository;

import com.esgi.blindTest.adapter.mapper.ParticipantEntityMapper;
import com.esgi.blindTest.domain.model.Participant;
import com.esgi.blindTest.domain.repository.ParticipantRepository;
import com.esgi.blindTest.infra.persistance.repository.ParticipantJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class ParticipantRepositoryImpl implements ParticipantRepository {

    private final ParticipantJpaRepository participantJpaRepository;
    private final ParticipantEntityMapper participantEntityMapper;

    @Override
    @Transactional
    public Participant save(Participant participant) {
        return participantEntityMapper.toEntity(
                participantJpaRepository.save(participantEntityMapper.toDto(participant)));
    }

    @Override
    @Transactional(readOnly = true)
    public Participant findByEmailAndMotDePasse(String email, String motDePasse) {
        return participantEntityMapper.toEntity(
                participantJpaRepository.findByEmailAndMotDePasse(email, motDePasse));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Participant> findByEmail(String email) {
        return participantJpaRepository.findByEmail(email).map(participantEntityMapper::toEntity);
    }
}

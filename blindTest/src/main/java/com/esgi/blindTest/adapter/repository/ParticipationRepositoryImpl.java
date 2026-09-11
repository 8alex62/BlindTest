package com.esgi.blindTest.adapter.repository;

import com.esgi.blindTest.adapter.mapper.ParticipationEntityMapper;
import com.esgi.blindTest.domain.exception.BlindTestIntrouvableException;
import com.esgi.blindTest.domain.exception.ParticipantIntrouvableException;
import com.esgi.blindTest.domain.exception.ParticipationIntrouvableException;
import com.esgi.blindTest.domain.model.Participation;
import com.esgi.blindTest.domain.repository.ParticipationRepository;
import com.esgi.blindTest.infra.persistance.entity.ParticipationEntity;
import com.esgi.blindTest.infra.persistance.repository.BlindTestJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.ParticipantJpaRepository;
import com.esgi.blindTest.infra.persistance.repository.ParticipationJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepository {

    private final ParticipationJpaRepository participationJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final BlindTestJpaRepository blindTestJpaRepository;
    private final ParticipationEntityMapper participationEntityMapper;

    @Override
    @Transactional
    public Participation enregistrer(String nomBlindTest, Participation participation) {
        ParticipationEntity entity = new ParticipationEntity();
        entity.setBlindTest(blindTestJpaRepository.findByNom(nomBlindTest)
                .orElseThrow(BlindTestIntrouvableException::new));
        entity.setParticipant(participantJpaRepository
                .findByEmail(participation.getParticipant().getEmail())
                .orElseThrow(ParticipantIntrouvableException::new));
        entity.setScore(participation.getScore());
        return participationEntityMapper.toEntity(participationJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public Participation ajouterUnPoint(String nomBlindTest, Participation participation) {
        ParticipationEntity entity = participationJpaRepository
                .findByBlindTestNomAndParticipantEmail(
                        nomBlindTest, participation.getParticipant().getEmail())
                .orElseThrow(ParticipationIntrouvableException::new);
        entity.setScore(participation.getScore());
        return participationEntityMapper.toEntity(participationJpaRepository.save(entity));
    }
}
